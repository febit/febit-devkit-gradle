/*
 * Copyright 2022-present febit.org (support@febit.org)
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
package org.febit.devkit.gradle.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DefaultsTest {

    @Test
    void returnsOriginalWhenNotNull() {
        var original = "value";
        assertSame(original, Defaults.nvl(original, "default"));
    }

    @Test
    void returnsIfAbsentWhenNull() {
        assertSame("default", Defaults.nvl(null, "default"));
    }

    @Test
    void returnsNullWhenBothNull() {
        assertNull(Defaults.nvl(null, null));
    }

    @Test
    void preservesType() {
        Integer original = 1;
        assertSame(original, Defaults.nvl(original, 2));
    }
}
