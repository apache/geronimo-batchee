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
package org.apache.batchee.test.tck.lifecycle;

import jakarta.batch.operations.BatchRuntimeException;
import jakarta.ejb.embeddable.EJBContainer;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.util.logging.Logger;

// forces the execution in embedded container
public class ContainerLifecycle implements TestExecutionListener {
    private EJBContainer container;
    private Logger logger = null;


    public void testPlanExecutionStarted(TestPlan testPlan) {
        final String loggerName = "test-lifecycle";

        container = EJBContainer.createEJBContainer(new PropertiesBuilder()
                .p("openejb.jul.forceReload", Boolean.TRUE.toString())
                .p("openejb.log.color", colors())
                .p(loggerName + ".level", "INFO")
                .p("openejb.jdbc.log", Boolean.FALSE.toString()) // with jdbc set it to TRUE to get sql queries

                .p("jdbc/orderDB", "new://Resource?type=DataSource")
                .p("jdbc/orderDB.JdbcDriver", EmbeddedDriver.class.getName())
                .p("jdbc/orderDB.JdbcUrl", "jdbc:derby:memory:orderDB" + ";create=true")
                .p("jdbc/orderDB.UserName", "app")
                .p("jdbc/orderDB.Password", "app")
                .p("jdbc/orderDB.JtaManaged", Boolean.TRUE.toString())

                .p("jdbc/batchee", "new://Resource?type=DataSource")
                .p("jdbc/batchee.JdbcDriver", EmbeddedDriver.class.getName())
                .p("jdbc/batchee.JdbcUrl", "jdbc:derby:memory:batchee" + ";create=true")
                .p("jdbc/batchee.UserName", "app")
                .p("jdbc/batchee.Password", "app")
                .p("jdbc/batchee.JtaManaged", Boolean.FALSE.toString())
                .build());

        logger = Logger.getLogger(loggerName);
    }

    public void testPlanExecutionFinished(TestPlan testPlan) {
        if (container != null) {
            try {
                container.close();
            } catch (final Exception e) {
                throw new BatchRuntimeException(e);
            }
        }
    }

    public void dynamicTestRegistered(TestIdentifier testIdentifier) {
    }

    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        logger.warning(">>> SKIPPED");
    }

    public void executionStarted(TestIdentifier testIdentifier) {
        logger.info("====================================================================================================");
        logger.info(testIdentifier.getDisplayName());
        logger.info("----------------------------------------------------------------------------------------------------");
    }

    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        final TestExecutionResult.Status status = testExecutionResult.getStatus();

        switch (status) {
            case FAILED:
                logger.severe(">>> FAILURE");
                break;
            case SUCCESSFUL:
                logger.info(">>> SUCCESS");
                break;
        }
    }

    public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
    }
//
    private String colors() {
        try {
            Thread.currentThread().getContextClassLoader().loadClass("org.fusesource.jansi.AnsiConsole");
            return Boolean.toString(!System.getProperty("os.name").toLowerCase().contains("win"));
        } catch (final ClassNotFoundException | NoClassDefFoundError e) {
            return "false";
        }
    }

}
