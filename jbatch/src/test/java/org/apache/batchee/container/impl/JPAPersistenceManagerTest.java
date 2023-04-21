package org.apache.batchee.container.impl;
/*
 * Copyright 2012 International Business Machines Corp.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import static org.junit.Assert.assertEquals;

import java.util.Properties;

import jakarta.batch.operations.NoSuchJobException;
import jakarta.batch.operations.NoSuchJobExecutionException;
import jakarta.batch.runtime.JobInstance;

import org.apache.batchee.container.services.InternalJobExecution;
import org.apache.batchee.container.services.ServicesManager;
import org.apache.batchee.container.services.persistence.JPAPersistenceManagerService;
import org.apache.batchee.spi.PersistenceManagerService;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("serial")
public class JPAPersistenceManagerTest {

    private static final String VALID_JOBNAME = "simple";
    private static final String INVALID_JOBNAME = "simple_batchee139";
    private static final int INVALID_ID = -1;
    private static final Properties simpleJobProp;

    private static JobOperatorImpl operator;
    private static long executionId;

    static {
        simpleJobProp = new Properties() {
            {
                setProperty("duration", "10");
            }
        };
    }

    @BeforeClass
    public static void setup() {
        operator = new JobOperatorImpl(new ServicesManager() {
            {
                init(new Properties() {
                    {
                        setProperty(PersistenceManagerService.class.getName(),
                                JPAPersistenceManagerService.class.getName());
                    }
                });
            }
        });
        executionId = triggerSimpleJob();
    }

    @Test(expected = NoSuchJobExecutionException.class)
    public void testGetJobExecutionError_BATCHEE139() {
        operator.getJobExecution(INVALID_ID);
    }

    @Test
    public void testGetJobExecution_BATCHEE139() {
        final InternalJobExecution jobExecution = operator.getJobExecution(executionId);
        assertEquals(executionId, jobExecution.getExecutionId());
    }

    @Test(expected = NoSuchJobExecutionException.class)
    public void testGetJobInstanceError_BATCHEE139() {
        operator.getJobInstance(INVALID_ID);
    }

    @Test
    public void testGetJobInstance_BATCHEE139() {
        final JobInstance jobInstance = operator.getJobInstance(executionId);
        assertEquals(operator.getJobExecution(executionId).getInstanceId(), jobInstance.getInstanceId());
    }

    @Test
    public void testGetParameters_BATCHEE139() {
        final Properties parameters = operator.getParameters(executionId);
        assertEquals(simpleJobProp, parameters);
    }

    @Test(expected = NoSuchJobExecutionException.class)
    public void testGetParametersError_BATCHEE139() {
        operator.getParameters(INVALID_ID);
    }

    @Test(expected = NoSuchJobException.class)
    public void testJobInstanceCountError_BATCHEE139() {
        operator.getJobInstanceCount(INVALID_JOBNAME);
    }

    private static long triggerSimpleJob() {
        return operator.start(VALID_JOBNAME, simpleJobProp);
    }

}
