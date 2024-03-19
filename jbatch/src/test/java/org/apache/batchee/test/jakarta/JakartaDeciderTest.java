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
package org.apache.batchee.test.jakarta;

import jakarta.batch.api.AbstractBatchlet;
import jakarta.batch.api.BatchProperty;
import jakarta.batch.api.Decider;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.StepExecution;
import jakarta.inject.Inject;
import org.apache.batchee.util.Batches;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Properties;

import static org.testng.Assert.assertEquals;

public class JakartaDeciderTest {


    @Test
    public void testDeciderRestart() {

        JobOperator jobOperator = BatchRuntime.getJobOperator();
        long executionId = jobOperator.start("decider-test-jakarta", new Properties());

        BatchStatus batchStatus = Batches.waitFor(jobOperator, executionId);
        assertEquals(batchStatus, BatchStatus.STOPPED);
        assertEquals(jobOperator.getJobExecution(executionId).getExitStatus(), "decider-stop");

        List<StepExecution> stepExecutions = jobOperator.getStepExecutions(executionId);
        assertEquals(stepExecutions.size(), 1);

        long restartExecutionId = jobOperator.restart(executionId, new Properties());

        BatchStatus restartStatus = Batches.waitFor(jobOperator, restartExecutionId);
        assertEquals(restartStatus, BatchStatus.COMPLETED);

        String exitStatus = jobOperator.getJobExecution(restartExecutionId).getExitStatus();
        assertEquals("COMPLETED", exitStatus);

        List<StepExecution> restartExecutions = jobOperator.getStepExecutions(restartExecutionId);
        assertEquals(restartExecutions.size(), 1);
        assertEquals(restartExecutions.get(0).getStepName(), "executeOnRestart");
    }


    public static class TheDecider implements Decider {

        @Override
        public String decide(StepExecution[] executions) throws Exception {
            return "foobar";
        }
    }

    public static class TheBatchlet extends AbstractBatchlet {

        @Inject
        @BatchProperty
        private String inStep;

        @Override
        public String process() throws Exception {
            return inStep;
        }
    }
}
