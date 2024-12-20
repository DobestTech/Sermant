/*
 * Copyright (C) 2023-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.core.command;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.utils.StringUtils;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CommandProcessor
 *
 * @author zhp
 * @since 2023-09-09
 */
public class CommandProcessor {
    /**
     * COMMAND_EXECUTOR_MAP
     */
    private static final Map<String, CommandExecutor> COMMAND_EXECUTOR_MAP = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String COMMAND = "command";

    private static Instrumentation instrumentation;

    static {
        COMMAND_EXECUTOR_MAP.put(Command.INSTALL_PLUGINS.getValue(), new PluginsInstallCommandExecutor());
        COMMAND_EXECUTOR_MAP.put(Command.UNINSTALL_AGENT.getValue(), new AgentUnInstallCommandExecutor());
        COMMAND_EXECUTOR_MAP.put(Command.UNINSTALL_PLUGINS.getValue(), new PluginsUnInstallCommandExecutor());
        COMMAND_EXECUTOR_MAP.put(Command.UPDATE_PLUGINS.getValue(), new PluginsUpdateCommandExecutor());
        COMMAND_EXECUTOR_MAP.put(Command.CHECK_ENHANCEMENT.getValue(), new CheckEnhancementsCommandExecutor());
        COMMAND_EXECUTOR_MAP.put(Command.INSTALL_EXTERNAL_AGENT.getValue(), new ExternalAgentInstallCommandExecutor());
    }

    /**
     * constructor
     */
    private CommandProcessor() {
    }

    /**
     * process command
     *
     * @param agentArgsMap agent args map for dynamic command
     */
    public static void process(Map<String, String> agentArgsMap) {
        String command = agentArgsMap.get(COMMAND);
        if (StringUtils.isEmpty(command)) {
            LOGGER.warning("Command information is empty.");
            return;
        }
        LOGGER.log(Level.INFO, "Command information is {0}.", command);
        String[] commandInfo = command.trim().split(":");
        if (commandInfo.length == 0) {
            LOGGER.warning("Illegal command information.");
            return;
        }
        CommandExecutor commandExecutor = COMMAND_EXECUTOR_MAP.get(commandInfo[0].toUpperCase(Locale.ROOT));
        if (commandExecutor == null) {
            LOGGER.warning("No corresponding command executor found.");
            return;
        }
        String commandArgs = commandInfo.length > 1 ? commandInfo[1] : null;
        DynamicAgentArgsManager.refreshAgentArgs(agentArgsMap);
        commandExecutor.execute(commandArgs);
    }

    /**
     * cache instrumentation for dynamic agent installation
     *
     * @param inst instrumentation
     */
    public static void cacheInstrumentation(Instrumentation inst) {
        instrumentation = inst;
    }

    /**
     * get instrumentation for dynamic agent installation
     *
     * @return instrumentation
     */
    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }
}
