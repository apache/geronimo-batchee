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
package org.apache.batchee.servlet;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import org.apache.batchee.container.services.ServicesManager;

@WebListener
public class CleanUpWebappListener implements ServletContextListener {
    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        // no-op
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        final ServicesManager servicesManager = ServicesManager.find();
        if (ServicesManager.class.getClassLoader() == sce.getServletContext().getClassLoader()) {
            servicesManager.close();
        }
    }
}
