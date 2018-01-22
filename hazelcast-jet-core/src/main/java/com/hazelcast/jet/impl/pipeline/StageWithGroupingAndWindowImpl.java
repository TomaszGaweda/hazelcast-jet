/*
 * Copyright (c) 2008-2017, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.jet.impl.pipeline;

import com.hazelcast.jet.aggregate.AggregateOperation1;
import com.hazelcast.jet.aggregate.AggregateOperation2;
import com.hazelcast.jet.aggregate.AggregateOperation3;
import com.hazelcast.jet.datamodel.TimestampedEntry;
import com.hazelcast.jet.function.DistributedFunction;
import com.hazelcast.jet.impl.pipeline.transform.CoGroupTransform;
import com.hazelcast.jet.impl.pipeline.transform.GroupTransform;
import com.hazelcast.jet.pipeline.StageWithGroupingAndWindow;
import com.hazelcast.jet.pipeline.StreamStage;
import com.hazelcast.jet.pipeline.StreamStageWithGrouping;
import com.hazelcast.jet.pipeline.WindowDefinition;
import com.hazelcast.jet.pipeline.WindowGroupAggregateBuilder;

import javax.annotation.Nonnull;

import static java.util.Arrays.asList;

public class StageWithGroupingAndWindowImpl<T, K>
        extends StageWithGroupingBase<T, K>
        implements StageWithGroupingAndWindow<T, K> {

    @Nonnull
    private final WindowDefinition wDef;

    StageWithGroupingAndWindowImpl(
            @Nonnull StreamStageImpl<T> computeStage,
            @Nonnull DistributedFunction<? super T, ? extends K> keyFn,
            @Nonnull WindowDefinition wDef
    ) {
        super(computeStage, keyFn);
        this.wDef = wDef;
    }

    @Nonnull @Override
    public WindowDefinition windowDefinition() {
        return wDef;
    }

    @Nonnull
    public <A, R> StreamStage<TimestampedEntry<K, R>> aggregate(
            @Nonnull AggregateOperation1<? super T, A, R> aggrOp
    ) {
        return computeStage.attach(
                new GroupTransform<T, K, A, R>(computeStage.transform, keyFn(),
                        computeStage.fnAdapters.adaptAggregateOperation1(aggrOp), windowDefinition()),
                computeStage.fnAdapters);
    }

    @Nonnull @Override
    public <T1, A, R> StreamStage<TimestampedEntry<K, R>> aggregate2(
            @Nonnull StreamStageWithGrouping<T1, ? extends K> stage1,
            @Nonnull AggregateOperation2<? super T, ? super T1, A, R> aggrOp
    ) {
        return computeStage.attach(
                new CoGroupTransform<K, A, R>(
                        asList(computeStage.transform, transformOf(stage1)),
                        asList(keyFn(), stage1.keyFn()), aggrOp, windowDefinition()
                ), computeStage.fnAdapters);
    }

    @Nonnull @Override
    public <T1, T2, A, R> StreamStage<TimestampedEntry<K, R>> aggregate3(
            @Nonnull StreamStageWithGrouping<T1, ? extends K> stage1,
            @Nonnull StreamStageWithGrouping<T2, ? extends K> stage2,
            @Nonnull AggregateOperation3<? super T, ? super T1, ? super T2, A, R> aggrOp
    ) {
        return computeStage.attach(
                new CoGroupTransform<K, A, R>(
                        asList(computeStage.transform, transformOf(stage1), transformOf(stage2)),
                        asList(keyFn(), stage1.keyFn(), stage2.keyFn()), aggrOp, windowDefinition()
                ), computeStage.fnAdapters);
    }

    @Nonnull @Override
    public WindowGroupAggregateBuilder<T, K> aggregateBuilder() {
        return new WindowGroupAggregateBuilder<>(this);
    }
}
