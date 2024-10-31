/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.router.spring.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.utils.LogUtils;
import io.sermant.core.utils.StringUtils;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.common.constants.RouterConstant;
import io.sermant.router.common.metric.MetricThreadLocal;
import io.sermant.router.common.metric.MetricsManager;
import io.sermant.router.common.request.RequestData;
import io.sermant.router.common.utils.FlowContextUtils;
import io.sermant.router.common.utils.ThreadLocalUtils;
import io.sermant.router.spring.wrapper.RequestCallbackWrapper;

import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Enhanced interceptor for org.springframework.web.client.RestTemplate<br>
 *
 * @author yuzl Yu Zhenlong
 * @since 2022-10-27
 */
public class RestTemplateInterceptor extends MarkInterceptor {
    private static final int CALLBACK_ARG_LENGTH = 3;

    private static final int CALLBACK_ARG_POSITION = 2;

    private final RouterConfig routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        LogUtils.printHttpRequestBeforePoint(context);
        Object[] arguments = context.getArguments();
        MetricThreadLocal.setFlag(true);
        if (arguments != null && arguments.length > CALLBACK_ARG_LENGTH) {
            Object argument = arguments[CALLBACK_ARG_POSITION];
            if (argument instanceof RequestCallbackWrapper) {
                RequestCallbackWrapper callback = (RequestCallbackWrapper) argument;
                parseTags(callback, arguments[0], arguments[1]);
            }
        }
        return context;
    }

    private void parseTags(RequestCallbackWrapper callback, Object url, Object method) {
        Map<String, String> header = callback.getHeader();
        if (StringUtils.isBlank(FlowContextUtils.getTagName())) {
            return;
        }
        String encodeTag = header.get(FlowContextUtils.getTagName());
        if (StringUtils.isBlank(encodeTag)) {
            return;
        }
        Map<String, List<String>> tags = FlowContextUtils.decodeTags(encodeTag);
        if (!tags.isEmpty()) {
            ThreadLocalUtils.setRequestData(getRequestData(tags, url, method));
        }
    }

    private RequestData getRequestData(Map<String, List<String>> tags, Object url, Object method) {
        String path = "";
        if (url instanceof URI) {
            path = ((URI) url).getPath();
        }
        String httpMethod = "";
        if (method instanceof HttpMethod) {
            httpMethod = ((HttpMethod) method).name();
        }
        return new RequestData(tags, path, httpMethod);
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        ThreadLocalUtils.removeRequestData();
        LogUtils.printHttpRequestAfterPoint(context);
        collectRequestCountMetric(context);
        return context;
    }

    private void collectRequestCountMetric(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        if (routerConfig.isEnableMetric() && MetricThreadLocal.getFlag() && arguments[0] instanceof URI) {
            MetricsManager.collectRequestCountMetric((URI) arguments[0]);
            context.setLocalFieldValue(RouterConstant.EXECUTE_FLAG, Boolean.TRUE);
        }
        MetricThreadLocal.removeFlag();
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        ThreadLocalUtils.removeRequestData();
        LogUtils.printHttpRequestOnThrowPoint(context);
        MetricThreadLocal.removeFlag();
        return context;
    }
}
