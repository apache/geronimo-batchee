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
package org.apache.batchee.cli.zip;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertTrue;

public class ZipsTest {
    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void zipSlip() throws IOException {
        final File zip = temp.newFile("test.zip");
        final String slipFile = "attack.txt";

        try (final ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip.toPath()))) {
            out.putNextEntry(new ZipEntry("../" + slipFile));
            out.write("test".getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        }

        final File exploded = temp.newFolder("some/nested/folder");
        Zips.unzip(zip, exploded);
        assertTrue(new File(exploded.getParent(), slipFile).exists());
    }
}
