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
package org.apache.batchee.jaxrs.client.provider;

import org.apache.batchee.jaxrs.client.http.Base64s;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Base64Filter implements ClientRequestFilter, Base64s {
    private final String user;
    private final String pwd;

    public Base64Filter(final String username, final String password) {
        this.user = username;
        this.pwd = password;
    }

    @Override
    public void filter(final ClientRequestContext requestContext) {
        final MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        headers.add(AUTHORIZATION_HEADER,
                BASIC_PREFIX + Base64.getEncoder().encodeToString((user + ":" + pwd).getBytes(UTF_8)));
    }
}
