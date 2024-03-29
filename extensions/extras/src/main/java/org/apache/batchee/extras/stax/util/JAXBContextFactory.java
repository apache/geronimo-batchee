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
package org.apache.batchee.extras.stax.util;

import jakarta.batch.operations.BatchRuntimeException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

public class JAXBContextFactory {
    public static JAXBContext getJaxbContext(final String marshallingPackage, final String marshallingClasses) throws JAXBException {
        if (marshallingPackage != null) {
            return JAXBContext.newInstance(marshallingPackage);
        }

        final String[] classesStr = marshallingClasses.split(",");
        final Class<?>[] classes = new Class<?>[classesStr.length];
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        for (int i = 0; i < classes.length; i++) {
            try {
                classes[i] = contextClassLoader.loadClass(classesStr[i]);
            } catch (final ClassNotFoundException e) {
                throw new BatchRuntimeException(e);
            }
        }
        return JAXBContext.newInstance(classes);
    }

    private JAXBContextFactory() {
        // no-op
    }
}
