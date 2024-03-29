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
package org.apache.batchee.jsefa;

import net.sf.jsefa.Deserializer;
import net.sf.jsefa.xml.XmlIOFactory;
import org.apache.batchee.doc.api.Documentation;

import jakarta.batch.api.BatchProperty;
import jakarta.inject.Inject;

@Documentation("Reads a XML file using JSefa.")
public class JSefaXmlReader extends JSefaReader {
    @Inject
    @BatchProperty
    @Documentation("low level configuration implementation")
    private String lowLevelConfiguration;

    @Inject
    @BatchProperty
    @Documentation("EOL")
    private String lineBreak;

    @Inject
    @BatchProperty
    @Documentation("registry name for data type")
    private String dataTypeDefaultNameRegistry;

    @Inject
    @BatchProperty
    @Documentation("indentation")
    private String lineIndentation;

    @Inject
    @BatchProperty
    @Documentation("namespace manager to use")
    private String namespaceManager;

    @Inject
    @BatchProperty
    @Documentation("data type attribute name")
    private String dataTypeAttributeName;

    @Override
    protected Deserializer initDeserializer() throws Exception {
        return XmlIOFactory.createFactory(
            JsefaConfigurations.newXmlConfiguration(lineBreak, dataTypeDefaultNameRegistry, lineIndentation,
                lowLevelConfiguration, namespaceManager, dataTypeAttributeName,
                validationMode, validationProvider, objectAccessorProvider,
                simpleTypeProvider, typeMappingRegistry),
            JsefaConfigurations.createObjectTypes(objectTypes))
            .createDeserializer();
    }
}
