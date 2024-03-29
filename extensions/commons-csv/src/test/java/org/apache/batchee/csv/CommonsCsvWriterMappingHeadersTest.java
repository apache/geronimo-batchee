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
package org.apache.batchee.csv;

import org.apache.batchee.csv.mapper.Csv;
import org.apache.batchee.csv.util.IOs;
import org.apache.batchee.util.Batches;
import org.testng.annotations.Test;

import jakarta.batch.api.chunk.ItemReader;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import java.io.Serializable;
import java.util.Properties;

import static org.testng.Assert.assertTrue;

public class CommonsCsvWriterMappingHeadersTest {
    @Test
    public void read() throws Exception {
        final String path = "target/work/CommonsCsvWriterMappingHeadersTest.txt";

        final Properties jobParams = new Properties();
        jobParams.setProperty("output", path);

        final JobOperator jobOperator = BatchRuntime.getJobOperator();
        Batches.waitForEnd(jobOperator, jobOperator.start("csv-writer-defaultmapperheaders", jobParams));
        final String output = IOs.slurp(path);

        assertTrue(output.contains("c1,c2"));
        assertTrue(output.contains("v1_,_v1__"));
        assertTrue(output.contains("v2_,_v2__"));
    }

    public static class TwoItemsReader implements ItemReader {
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
                return new Record("v" + count + "_");
            }
            return null;
        }

        @Override
        public Serializable checkpointInfo() throws Exception {
            return null;
        }
    }

    private static class Record {
        @Csv(index = 0, name = "c1")
        private final String value1;

        @Csv(index = 1, name = "c2")
        private final String value2;

        public Record(final String s) {
            this.value1 = s;
            this.value2 = "_" + s + "_";
        }
    }
}
