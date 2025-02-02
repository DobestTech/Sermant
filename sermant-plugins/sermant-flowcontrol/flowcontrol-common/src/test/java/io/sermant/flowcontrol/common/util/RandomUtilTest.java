/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.flowcontrol.common.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * RandomUtil test
 *
 * @author zhp
 * @since 2024-12-05
 */
public class RandomUtilTest {
    @Test
    public void testRandomInt1() {
        assertEquals(0, RandomUtil.randomInt(0, 1));
    }

    @Test
    public void testRandomInt2() {
        assertEquals(0, RandomUtil.randomInt(1));
    }
}
