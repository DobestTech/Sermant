/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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
 *
 */

package io.sermant.flowcontrol.res4j.chain.handler;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.res4j.chain.HandlerConstants;
import io.sermant.flowcontrol.res4j.chain.context.ChainContext;
import io.sermant.flowcontrol.res4j.chain.context.RequestContext;
import io.sermant.flowcontrol.res4j.handler.BulkheadHandler;

import java.util.List;

/**
 * isolation bin treatment
 *
 * @author zhouss
 * @since 2022-07-05
 */
public class BulkheadRequestHandler extends FlowControlHandler<Bulkhead> {
    private final BulkheadHandler bulkheadHandler = new BulkheadHandler();

    @Override
    public void onBefore(RequestContext context, FlowControlScenario flowControlScenario) {
        final List<Bulkhead> handlers =
                bulkheadHandler.createOrGetHandlers(flowControlScenario.getMatchedScenarioNames());
        if (!handlers.isEmpty()) {
            context.save(getContextName(), handlers);
            handlers.forEach(Bulkhead::acquirePermission);
        }
        super.onBefore(context, flowControlScenario);
    }

    @Override
    public void onThrow(RequestContext context, FlowControlScenario flowControlScenario, Throwable throwable) {
        super.onThrow(context, flowControlScenario, throwable);
    }

    @Override
    public void onResult(RequestContext context, FlowControlScenario flowControlScenario, Object result) {
        try {
            final List<Bulkhead> bulkheads = getHandlersFromCache(context.getSourceName(), getContextName());
            if (bulkheads != null && !isOccurBulkheadLimit(context.getSourceName())) {
                bulkheads.forEach(Bulkhead::onComplete);
            }
        } finally {
            context.remove(getContextName());
        }
        super.onResult(context, flowControlScenario, result);
    }

    /**
     * 是否触发隔离仓策略
     *
     * @param sourceName 线程变量的名称
     * @return 若触发隔离仓则无需释放资源
     */
    private boolean isOccurBulkheadLimit(String sourceName) {
        return ChainContext.getThreadLocalContext(sourceName)
                .get(HandlerConstants.OCCURRED_FLOW_EXCEPTION, Exception.class) instanceof BulkheadFullException;
    }

    @Override
    public int getOrder() {
        return HandlerConstants.BULK_HEAD_ORDER;
    }
}
