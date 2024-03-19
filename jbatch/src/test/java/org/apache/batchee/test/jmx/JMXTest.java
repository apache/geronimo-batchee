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
package org.apache.batchee.test.jmx;

import org.apache.batchee.container.services.ServicesManager;
import org.apache.batchee.jmx.BatchEEMBean;
import org.apache.batchee.spi.PersistenceManagerService;
import org.apache.batchee.test.lifecyle.ContainerLifecycle;
import org.apache.batchee.util.Batches;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.JobInstance;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Listeners(ContainerLifecycle.class)
public class JMXTest {
    private static long executionId;
    private static long instanceId;
    private static long stepExecutionId;
    private static MBeanServer server;
    private static ObjectName on;

    @BeforeClass
    public static void createAJob() throws Exception {
        server = ManagementFactory.getPlatformMBeanServer();
        on = new ObjectName(BatchEEMBean.DEFAULT_OBJECT_NAME);

        final JobOperator jobOperator = BatchRuntime.getJobOperator();
        clearPersistence(jobOperator);

        executionId = jobOperator.start("jmx", new Properties() {{ setProperty("foo", "bar"); }});
        instanceId = jobOperator.getJobInstance(executionId).getInstanceId();
        Batches.waitForEnd(jobOperator, executionId);
        stepExecutionId = jobOperator.getStepExecutions(executionId).get(0).getStepExecutionId();
    }

    private static void clearPersistence(final JobOperator jobOperator) {
        final PersistenceManagerService service = ServicesManager.find().service(PersistenceManagerService.class);
        for (final String name : jobOperator.getJobNames()) {
            for (final JobInstance id : jobOperator.getJobInstances(name, 0, Integer.MAX_VALUE)) {
                service.cleanUp(id.getInstanceId());
            }
        }
    }

    @AfterClass
    public static void deleteJob() throws Exception {
        ServicesManager.find().service(PersistenceManagerService.class).cleanUp(instanceId);
    }

    private static Object attr(final String name) throws Exception {
        return server.getAttribute(on, name);
    }

    private static Object result(final String name, final Object... params) throws Exception {
        final String[] signature = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            signature[i] = params[i].getClass().getName().replace(Integer.class.getName(), int.class.getName()).replace(Long.class.getName(), long.class.getName());
        }
        return server.invoke(on, name, params, signature);
    }

    @Test
    public void jobNames() throws Exception {
        final String[] names = String[].class.cast(attr("JobNames"));
        assertNotNull(names);
        assertEquals(names.length, 1);
        assertEquals("jmx", names[0]);
    }

    @Test
    public void jobInstanceCount() throws Exception {
        assertEquals(Integer.class.cast(result("getJobInstanceCount", "jmx")).intValue(), 1);
    }

    @Test
    public void runningExecutions() throws Exception {
        assertEquals(Long[].class.cast(result("getRunningExecutions", "jmx")).length, 0);
    }

    @Test
    public void jobInstances() throws Exception {
        final TabularData instance = TabularData.class.cast(result("getJobInstances", "jmx", 0, 1));
        assertEquals(1, instance.size());

        final CompositeData cd = instance.get(new Object[]{"jmx", instanceId});
        assertEquals(instanceId, cd.get("instanceId"));
        assertEquals("jmx", cd.get("jobName"));
    }

    @Test
    public void parameters() throws Exception {
        final TabularData instance = TabularData.class.cast(result("getParameters", executionId));
        assertEquals(1, instance.size());

        final CompositeData cd = instance.get(List.class.cast(instance.keySet().iterator().next()).toArray());
        assertEquals("foo", cd.get("key"));
        assertEquals("bar", cd.get("value"));
    }

    @Test
    public void jobInstance() throws Exception {
        final TabularData instance = TabularData.class.cast(result("getJobInstance", executionId));
        assertEquals(1, instance.size());

        final CompositeData cd = instance.get(new Object[]{"jmx", instanceId});
        assertEquals(instanceId, cd.get("instanceId"));
        assertEquals("jmx", cd.get("jobName"));
    }

    @Test
    public void jobExecutions() throws Exception {
        final TabularData instance = TabularData.class.cast(result("getJobExecutions", instanceId, "jmx"));
        assertEquals(1, instance.size());

        final CompositeData cd = instance.get(List.class.cast(instance.keySet().iterator().next()).toArray());
        assertEquals(executionId, cd.get("executionId"));
        assertEquals("jmx", cd.get("jobName"));
        assertEquals("COMPLETED", cd.get("Exit status"));
        assertEquals("COMPLETED", cd.get("Batch status"));
    }

    @Test
    public void jobExecution() throws Exception {
        final TabularData instance = TabularData.class.cast(result("getJobExecution", executionId));
        assertEquals(1, instance.size());

        final CompositeData cd = instance.get(List.class.cast(instance.keySet().iterator().next()).toArray());
        assertEquals(executionId, cd.get("executionId"));
        assertEquals("jmx", cd.get("jobName"));
        assertEquals("COMPLETED", cd.get("Exit status"));
        assertEquals("COMPLETED", cd.get("Batch status"));
    }

    @Test
    public void stepExecutions() throws Exception {
        final TabularData instance = TabularData.class.cast(result("getStepExecutions", executionId));
        assertEquals(1, instance.size());

        final CompositeData cd = instance.get(List.class.cast(instance.keySet().iterator().next()).toArray());
        assertEquals(stepExecutionId, cd.get("stepExecutionId"));
        assertEquals("jmx-step", cd.get("stepName"));
        assertEquals("mock", cd.get("Exit status"));
        assertEquals("COMPLETED", cd.get("Batch status"));
        assertEquals(0L, cd.get("Commit"));
    }
}
