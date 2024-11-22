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

package io.sermant.core.plugin.config;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.config.ConfigManager;
import io.sermant.core.config.common.BaseConfig;
import io.sermant.core.config.utils.ConfigKeyUtil;
import io.sermant.core.plugin.Plugin;
import io.sermant.core.plugin.common.PluginConstant;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plugin Configuration Manager for loading plugin package configurations, a specialization of the unified configuration
 * manager ${ConfigManager}
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-12
 */
public class PluginConfigManager {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * configuration object map, key is plugin config key and the value is BaseConfig object
     */
    private static final Map<String, BaseConfig> PLUGIN_CONFIG_MAP = new HashMap<>();

    private PluginConfigManager() {
    }

    /**
     * Load plugin configuration
     *
     * @param plugin plugin
     */
    public static void loadPluginConfigs(Plugin plugin) {
        File pluginConfigFile = getPluginConfigFile(plugin.getPath());
        ClassLoader classLoader = plugin.getPluginClassLoader();

        for (BaseConfig config : ServiceLoader.load(PluginConfig.class, classLoader)) {
            processConfig(plugin, pluginConfigFile, config);
        }
    }

    private static void processConfig(Plugin plugin, File pluginConfigFile, BaseConfig config) {
        Class<? extends BaseConfig> pluginConfigCls = config.getClass();
        String pluginConfigKey = ConfigKeyUtil.getTypeKeyWithClassloader(ConfigKeyUtil.getTypeKey(pluginConfigCls),
                pluginConfigCls.getClassLoader());

        if (pluginConfigFile.isFile()) {
            handleFileConfig(plugin, pluginConfigFile, config, pluginConfigCls, pluginConfigKey);
        } else {
            loadDefaultConfig(plugin, config, pluginConfigKey);
        }
    }

    private static void handleFileConfig(Plugin plugin, File pluginConfigFile, BaseConfig config,
            Class<? extends BaseConfig> pluginConfigCls, String pluginConfigKey) {
        final BaseConfig retainedConfig = PLUGIN_CONFIG_MAP.get(pluginConfigKey);
        if (retainedConfig == null) {
            PLUGIN_CONFIG_MAP.put(pluginConfigKey, ConfigManager.doLoad(pluginConfigFile, config, plugin.isDynamic()));
            plugin.getConfigs().add(pluginConfigKey);
        } else if (retainedConfig.getClass() == pluginConfigCls) {
            LOGGER.log(Level.FINE, "Skip load config [{0}] repeatedly.", pluginConfigCls.getName());
        } else {
            LOGGER.log(Level.WARNING, "Type key of {0} is {1}, same as {2}'s.",
                    new String[]{pluginConfigCls.getName(), pluginConfigKey, retainedConfig.getClass().getName()});
        }
    }

    private static void loadDefaultConfig(Plugin plugin, BaseConfig config, String pluginConfigKey) {
        if (!PLUGIN_CONFIG_MAP.containsKey(pluginConfigKey)) {
            PLUGIN_CONFIG_MAP.put(pluginConfigKey, config);
            plugin.getConfigs().add(pluginConfigKey);
        }
    }

    /**
     * Clear the plugin's configuration cache
     *
     * @param plugin plugin
     */
    public static void cleanPluginConfigs(Plugin plugin) {
        for (String configName : plugin.getConfigs()) {
            PLUGIN_CONFIG_MAP.remove(configName);
        }
    }

    /**
     * The plugin configuration will not be initialized if the plugin configuration file does not exist. This method
     * will return a default object for this case
     *
     * @param cls Plugin configuration class
     * @param <R> Plugin configuration type
     * @return PluginConfig instance
     */
    public static <R extends PluginConfig> R getPluginConfig(Class<R> cls) {
        String pluginConfigKey = ConfigKeyUtil.getTypeKeyWithClassloader(ConfigKeyUtil.getTypeKey(cls),
                cls.getClassLoader());
        return (R) PLUGIN_CONFIG_MAP.get(pluginConfigKey);
    }

    /**
     * Get the plugin configuration file
     *
     * @param pluginPath plugin root directory
     * @return plugin configuration file
     */
    public static File getPluginConfigFile(String pluginPath) {
        return new File(pluginPath + File.separatorChar + PluginConstant.CONFIG_DIR_NAME + File.separatorChar
                + PluginConstant.CONFIG_FILE_NAME);
    }
}
