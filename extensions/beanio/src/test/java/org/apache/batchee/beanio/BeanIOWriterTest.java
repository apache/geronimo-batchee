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
package org.apache.batchee.beanio;

import org.apache.batchee.beanio.bean.Record;
import org.apache.batchee.beanio.util.IOs;
import org.apache.batchee.util.Batches;
import org.testng.annotations.Test;

import jakarta.batch.api.chunk.ItemReader;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import java.io.Serializable;
import java.util.Properties;

import static org.testng.Assert.assertTrue;

public class BeanIOWriterTest {
    @Test
    public void read() throws Exception {
        final String path = "target/work/BeanIOWriter.txt";

        final Properties jobParams = new Properties();
        jobParams.setProperty("output", path);

        final JobOperator jobOperator = BatchRuntime.getJobOperator();
        Batches.waitForEnd(jobOperator, jobOperator.start("beanio-writer", jobParams));
        final String output = IOs.slurp(path);

        assertTrue(output.contains("v1_2,v1_1"));
        assertTrue(output.contains("v2_2,v2_1"));
    }

    public static class TwoItemsReader implements ItemReader {
        private int count = 0;

        @Override
        public void open(Serializable checkpoint) throws Exception {
            // no-op
        }

        @Override
        public void close() throws Exception {
            // no-op
        }

        @Override
        public Object readItem() throws Exception {
            if (count++ < 2) {
                return new Record("v" + count + "_");
            }
            return null;
        }

        @Override
        public Serializable checkpointInfo() throws Exception {
            return null;
        }
    }
}
