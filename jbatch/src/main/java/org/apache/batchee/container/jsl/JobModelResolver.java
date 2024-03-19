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
package org.apache.batchee.container.jsl;

import org.apache.batchee.jaxb.JSLJob;

import jakarta.batch.operations.BatchRuntimeException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class JobModelResolver {
    private static final JAXBContext JOB_CONTEXT;

    static {
        try {
            JOB_CONTEXT = JAXBContext.newInstance(JSLJob.class.getPackage().getName());
        } catch (final JAXBException e) {
            throw new BatchRuntimeException(e);
        }
    }

    private JSLJob unmarshalJobXML(final String jobXML) {
        final JSLJob result;
        final JSLValidationEventHandler handler = new JSLValidationEventHandler();
        try(InputStream is = new ByteArrayInputStream(jobXML.getBytes(StandardCharsets.UTF_8))) {
            final InputSource src = new InputSource(is);

            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);

            final SAXParser parser = factory.newSAXParser();
            final Unmarshaller u = JOB_CONTEXT.createUnmarshaller();
            u.setSchema(Xsds.jobXML());
            u.setEventHandler(handler);

            final JakartaFilter xmlFilter = new JakartaFilter(parser.getXMLReader());
            xmlFilter.setContentHandler(u.getUnmarshallerHandler());

            result = u.unmarshal( new SAXSource(xmlFilter, src), JSLJob.class).getValue();
        } catch (final JAXBException | ParserConfigurationException | IOException | SAXException e) {
            throw new IllegalArgumentException("Exception unmarshalling jobXML", e);
        }
        if (handler.eventOccurred()) {
            throw new IllegalArgumentException("xJCL invalid per schema");
        }
        return result;
    }

    public JSLJob resolveModel(final String jobXML) {
        if (System.getSecurityManager() == null) {
            return unmarshalJobXML(jobXML);
        }
        return AccessController.doPrivileged(
            new PrivilegedAction<JSLJob>() {
                public JSLJob run() {
                    return unmarshalJobXML(jobXML);
                }
            });
    }

    private static class JakartaFilter extends XMLFilterImpl {
        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        public JakartaFilter(final XMLReader xmlReader) {
            super(xmlReader);
        }

        private String fixUri(final String uri) {
            if ("http://xmlns.jcp.org/xml/ns/javaee".equals(uri) || "http://jakarta.ee/xml/ns/jakartaee".equals(uri)){
                return "https://jakarta.ee/xml/ns/jakartaee";
            }
            return uri;
        }

        private Attributes fixVersion(final String localName, final Attributes atts) {
            if (localName.equals("job") && atts.getIndex("version") != -1 && !atts.getValue(atts.getIndex("version")).equals("2.0")) {
                final AttributesImpl newAtts = new AttributesImpl(atts);
                newAtts.setValue(newAtts.getIndex("version"), "2.0");
                return newAtts;
            }
            return atts;
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
            super.startElement(fixUri(uri), localName, qName, fixVersion(localName, atts));
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            super.endElement(fixUri(uri), localName, qName);
        }

        @Override
        public InputSource resolveEntity(final String publicId, final String systemId) {
            return EMPTY_INPUT_SOURCE;
        }
    }

}
