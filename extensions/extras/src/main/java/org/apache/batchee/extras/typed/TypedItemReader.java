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
package org.apache.batchee.extras.typed;

import jakarta.batch.api.chunk.ItemReader;
import java.io.Serializable;

/**
 * Typesafe abstraction of an ItemReader.
 *
 * @param <R> The type of the item returned in {@link #readItem()}.
 * @param <C> The type of the Checkpoint. See {@link #doCheckpointInfo()} and {@link #doRead()}
 */
public abstract class TypedItemReader<R, C extends Serializable> implements ItemReader {
    protected abstract void doOpen(C checkpoint);
    protected abstract R doRead();
    protected abstract C doCheckpointInfo();

    @Override
    public void open(Serializable checkpoint) throws Exception {
        doOpen((C) checkpoint);
    }

    @Override
    public Object readItem() throws Exception {
        return doRead();
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return doCheckpointInfo();
    }
}
