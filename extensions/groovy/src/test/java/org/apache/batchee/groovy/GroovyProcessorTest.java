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

import org.apache.batchee.groovy.util.IOs;
import org.apache.batchee.util.Batches;
import org.testng.annotations.Test;

import jakarta.batch.api.chunk.ItemReader;
import jakarta.batch.api.chunk.ItemWriter;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.testng.Assert.assertEquals;

public class GroovyProcessorTest {
    public static final List<Object> ITEMS = new ArrayList<Object>(2);

    @Test
    public void process() {
        IOs.write("target/work/processor.groovy", "package org.apache.batchee.groovy\n" +
            "\n" +
            "import jakarta.batch.api.chunk.ItemProcessor\n" +
            "\n" +
            "class GProcessor implements ItemProcessor {\n" +
            "    @Override\n" +
            "    Object processItem(final Object item) throws Exception {\n" +
            "        GroovyProcessorTest.ITEMS << item\n" +
            "        item\n" +
            "    }\n" +
            "}\n");
        final JobOperator operator = BatchRuntime.getJobOperator();
        Batches.waitForEnd(operator, operator.start("groovy-processor", new Properties()));
        assertEquals(ITEMS.size(), 2);
        assertEquals("g_1", ITEMS.get(0));
        assertEquals("g_2", ITEMS.get(1));
    }

    public static class Writer implements ItemWriter {
        @Override
        public void open(final Serializable checkpoint) throws Exception {
            // no-op
        }

        @Override
        public void close() throws Exception {
            // no-op
        }

        @Override
        public void writeItems(final List<Object> items) throws Exception {
            // no-op
        }

        @Override
        public Serializable checkpointInfo() throws Exception {
            return null;
        }
    }

    public static class Reader implements ItemReader {
        private int count = 0;

        @Override
        public void open(final Serializable checkpoint) throws Exception {
            // no-op
        }

        @Override
        public void close() throws Exception {
            // no-op
        }

        @Override
        public Object readItem() throws Exception {
            if (count++ < 2) {
                return "g_" + count;
            }
            return null;
        }

        @Override
        public Serializable checkpointInfo() throws Exception {
            return null;
        }
    }
}
