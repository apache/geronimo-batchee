/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.batchee.container.services.transaction;

import java.util.Properties;

import jakarta.batch.runtime.context.StepContext;

import org.apache.batchee.container.exception.BatchContainerServiceException;
import org.apache.batchee.container.exception.TransactionManagementException;
import org.apache.batchee.spi.TransactionManagementService;
import org.apache.batchee.spi.TransactionManagerAdapter;

public class NoTxMgrBatchTransactionService implements TransactionManagementService {
    private final TransactionManagerAdapter adapter = new DefaultNonTransactionalManager();

    @Override
    public void init(final Properties batchConfig) throws BatchContainerServiceException {
        // no-op
    }

    @Override
    public TransactionManagerAdapter getTransactionManager(final StepContext stepContext) throws TransactionManagementException {
        return adapter;
    }
}
