/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jet.pipeline;

import com.hazelcast.jet.aggregate.AggregateOperation;
import com.hazelcast.jet.datamodel.Tag;
import com.hazelcast.jet.impl.pipeline.AggBuilder;
import com.hazelcast.jet.impl.pipeline.AggBuilder.CreateOutStageFn;
import com.hazelcast.jet.impl.pipeline.BatchStageImpl;

public class AggregateBuilder<T0> {
    private final AggBuilder<T0> aggBuilder;

    AggregateBuilder(BatchStage<T0> s) {
        this.aggBuilder = new AggBuilder<>(s, null);
    }

    public Tag<T0> tag0() {
        return Tag.tag0();
    }

    @SuppressWarnings("unchecked")
    public <E> Tag<E> add(BatchStage<E> stage) {
        return aggBuilder.add(stage);
    }

    @SuppressWarnings("unchecked")
    public <A, R, OUT> BatchStage<OUT> build(
            AggregateOperation<A, R> aggrOp
    ) {
        CreateOutStageFn<OUT, BatchStage<OUT>> createOutStageFn = BatchStageImpl::new;
        return aggBuilder.build(aggrOp, createOutStageFn, null);
    }
}
