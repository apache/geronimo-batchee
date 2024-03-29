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
package org.apache.batchee.container.proxy;

import org.apache.batchee.container.exception.BatchContainerRuntimeException;

import jakarta.batch.api.chunk.CheckpointAlgorithm;

public class CheckpointAlgorithmProxy extends AbstractProxy<CheckpointAlgorithm> implements CheckpointAlgorithm {
    /*
     * Allow this to be public as a special case so we can easily treat the built-in algorithms
     * as identical to custom ones.
     */
    public CheckpointAlgorithmProxy(final CheckpointAlgorithm delegate) {
        super(delegate);
    }

    @Override
    public void beginCheckpoint() {
        try {
            this.delegate.beginCheckpoint();
        } catch (Exception e) {
            this.stepContext.setException(e);
            throw new BatchContainerRuntimeException(e);
        }
    }

    @Override
    public int checkpointTimeout() {
        try {
            return this.delegate.checkpointTimeout();
        } catch (Exception e) {
            this.stepContext.setException(e);
            throw new BatchContainerRuntimeException(e);
        }
    }

    @Override
    public void endCheckpoint() {
        try {
            this.delegate.endCheckpoint();
        } catch (Exception e) {
            this.stepContext.setException(e);
            throw new BatchContainerRuntimeException(e);
        }
    }

    @Override
    public boolean isReadyToCheckpoint() {
        try {
            return this.delegate.isReadyToCheckpoint();
        } catch (Exception e) {
            this.stepContext.setException(e);
            throw new BatchContainerRuntimeException(e);
        }
    }
}
