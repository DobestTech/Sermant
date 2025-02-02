/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.backend.entity.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration Center Information
 *
 * @author zhp
 * @since 2024-05-16
 */
@Getter
@Setter
public class ConfigServerInfo {
    /**
     * Configuration Center Address
     */
    private String serverAddress;

    /**
     * userName, authentication for Configuration Center
     */
    private String userName;

    /**
     * password, authentication for Configuration Center
     */
    private String password;

    /**
     * secret key, used for encryption and decryption of passwords
     */
    private String secretKey;

    /**
     * Type of Configuration Center
     */
    private String dynamicConfigType;

    /**
     * The namespace to which the configuration item belongs
     */
    private String namespace;
}
