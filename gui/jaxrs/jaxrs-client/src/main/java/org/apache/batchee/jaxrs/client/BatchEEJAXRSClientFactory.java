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
package org.apache.batchee.jaxrs.client;

import jakarta.batch.operations.JobOperator;
import java.io.Closeable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public final class BatchEEJAXRSClientFactory {
    private static final Class<?>[] PROXY_API = new Class<?>[]{JobOperator.class, Closeable.class};

    @Deprecated // we only use jaxrs 2 now
    public enum API {
        CXF,
        JAXRS2,
        AUTO
    }

    public static JobOperator newClient(final ClientConfiguration configuration, final API api) {
        final InvocationHandler handler = new BatchEEJAXRS2Client(configuration);
        return JobOperator.class.cast(
                Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), PROXY_API, handler));
    }

    public static JobOperator newClient(final ClientConfiguration configuration) {
        return newClient(configuration, API.AUTO);
    }

    public static JobOperator newClient(final String baseUrl) {
        final ClientConfiguration configuration = new ClientConfiguration();
        configuration.setBaseUrl(baseUrl);
        return newClient(configuration, API.AUTO);
    }

    private BatchEEJAXRSClientFactory() {
        // no-op
    }
}
