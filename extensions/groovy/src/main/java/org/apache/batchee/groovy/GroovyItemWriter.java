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
package org.apache.batchee.groovy;

import org.apache.batchee.doc.api.Documentation;

import jakarta.batch.api.BatchProperty;
import jakarta.batch.api.chunk.ItemWriter;
import jakarta.batch.runtime.context.JobContext;
import jakarta.batch.runtime.context.StepContext;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.util.List;

@Documentation("Reads and executes a writer from a groovy script")
public class GroovyItemWriter implements ItemWriter {
    @Inject
    @BatchProperty
    @Documentation("The script to execute")
    private String scriptPath;

    @Inject
    private JobContext jobContext;

    @Inject
    private StepContext stepContext;

    private ItemWriter delegate;
    private Groovys.GroovyInstance<ItemWriter> groovyInstance;

    @Override
    public void open(final Serializable checkpoint) throws Exception {
        groovyInstance = Groovys.newInstance(ItemWriter.class, scriptPath, jobContext, stepContext);
        delegate = groovyInstance.getInstance();
        delegate.open(checkpoint);
    }

    @Override
    public void close() throws Exception {
        delegate.close();
        groovyInstance.release();
    }

    @Override
    public void writeItems(final List<Object> items) throws Exception {
        delegate.writeItems(items);
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return delegate.checkpointInfo();
    }
}
