/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.plugin.config;

import static com.huaweicloud.sermant.core.plugin.common.PluginConstant.CONFIG_DIR_NAME;
import static com.huaweicloud.sermant.core.plugin.common.PluginConstant.CONFIG_FILE_NAME;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.Plugin;

import java.io.File;

/**
 * 插件配置管理器，${ConfigManager}统一配置管理器的特化，专门用来加载插件包配置和插件服务包配置
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-12
 */
public class PluginConfigManager extends ConfigManager {
    /**
     * 加载插件配置
     *
     * @param plugin 插件
     */
    public static void loadPluginConfig(Plugin plugin) {
        File pluginConfigFile = getPluginConfigFile(plugin.getPath());
        ClassLoader classLoader =
                plugin.getServiceClassLoader() != null ? plugin.getServiceClassLoader() : plugin.getPluginClassLoader();
        loadServiceConfig(pluginConfigFile, classLoader);
    }

    /**
     * 加载插件服务包配置
     *
     * @param configFile 配置文件夹
     * @param classLoader 加载插件服务包的类加载器
     */
    public static void loadServiceConfig(File configFile, ClassLoader classLoader) {
        loadConfig(configFile, PluginConfig.class, classLoader);
    }

    /**
     * 插件端专用的获取配置方法，当插件配置文件不存在时，插件配置将会不初始化出来，该方法将针对这一情况返回一个默认对象
     *
     * @param cls 插件配置类
     * @param <R> 插件配置类型
     * @return 插件配置实例
     */
    public static <R extends PluginConfig> R getPluginConfig(Class<R> cls) {
        return getConfig(cls);
    }

    /**
     * 获取插件配置文件
     *
     * @param pluginPath 插件根目录
     * @return 插件配置文件
     */
    public static File getPluginConfigFile(String pluginPath) {
        return new File(pluginPath + File.separatorChar + CONFIG_DIR_NAME + File.separatorChar + CONFIG_FILE_NAME);
    }
}
