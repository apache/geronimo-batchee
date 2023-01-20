/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.batchee.container.impl;

import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import jakarta.batch.api.chunk.AbstractItemReader;
import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.JobExecution;
import jakarta.batch.runtime.StepExecution;

import org.apache.batchee.container.services.ServicesManager;
import org.apache.batchee.container.services.persistence.MemoryPersistenceManagerService;
import org.apache.batchee.container.services.transaction.NoTxMgrBatchTransactionService;
import org.apache.batchee.spi.PersistenceManagerService;
import org.apache.batchee.spi.TransactionManagementService;
import org.apache.batchee.util.Batches;
import org.testng.annotations.Test;

public class ChunkStepControllerTest {
    @Test
    public void earlyStop() throws InterruptedException {
        final JobOperator operator = new JobOperatorImpl(new ServicesManager() {{
            init(new Properties() {{
                setProperty(PersistenceManagerService.class.getSimpleName(), MemoryPersistenceManagerService.class.getName());
                setProperty(TransactionManagementService.class.getSimpleName(), NoTxMgrBatchTransactionService.class.getName());
            }});
        }});

        final long id = operator.start("stop-chunk", new Properties());
        Reader.LATCH.await();
        operator.stop(id);
        Batches.waitFor(operator, id);
        final List<StepExecution> stepExecutions = operator.getStepExecutions(id);
        final StepExecution stepExecution = stepExecutions.iterator().next();
        assertEquals("STOPPED", stepExecution.getExitStatus()); // before (BATCHEE-138) it was FAILED
    }

    public static class Reader extends AbstractItemReader {
        static final CountDownLatch LATCH = new CountDownLatch(8);

        @Override
        public Object readItem() throws Exception {
            LATCH.countDown();
            return new Object();
        }
    }

    public static class Writer extends AbstractItemWriter {
        @Override
        public void writeItems(final List<Object> list) {
            // no-op
        }
    }
}
