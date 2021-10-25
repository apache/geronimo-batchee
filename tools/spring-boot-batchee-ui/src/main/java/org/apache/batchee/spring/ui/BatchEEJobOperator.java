/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.batchee.spring.ui;

import javax.batch.operations.JobOperator;
import javax.batch.operations.JobSecurityException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.enumeration;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;

public class BatchEEJobOperator extends DelegatingJobOperator {
    private final String[] defaultJobNames;

    public BatchEEJobOperator(final JobOperator builtIn,
                              final String... defaultJobNames) {
        super(builtIn);
        this.defaultJobNames = defaultJobNames;
    }

    @Override
    public Set<String> getJobNames() throws JobSecurityException {
        final Set<String> builtInNames = super.getJobNames();
        return defaultJobNames.length == 0 ?
                builtInNames :
                Stream.concat(Stream.of(defaultJobNames), builtInNames.stream()).collect(toSet());
    }

    public static class Context {
        private Context() {
            // no-op
        }

        public static <T> T withFacade(final JobOperator operator, final Function<JobOperator, T> task) {
            JobOperatorFacade.current = operator;
            final Thread thread = Thread.currentThread();
            final ClassLoader loader = thread.getContextClassLoader();
            thread.setContextClassLoader(new ClassLoader(loader) {
                // don't use default spring JobOperator impl, it is not accurate and loads a global context - we don't want it
                @Override
                public Enumeration<URL> getResources(final String name) throws IOException {
                    if ("META-INF/services/javax.batch.operations.JobOperator".equals(name)) {
                        return enumeration(singleton(new URL("embed://", null, -1, "", new URLStreamHandler() {
                            @Override
                            protected URLConnection openConnection(final URL u) {
                                return new URLConnection(u) {
                                    @Override
                                    public void connect() {
                                        // no-op
                                    }

                                    @Override
                                    public InputStream getInputStream() {
                                        return new ByteArrayInputStream(JobOperatorFacade.class.getName().getBytes(StandardCharsets.UTF_8));
                                    }
                                };
                            }
                        })));
                    }
                    return super.getResources(name);
                }
            });
            try {
                return task.apply(operator);
            } finally {
                thread.setContextClassLoader(loader);
            }
        }
    }
}
