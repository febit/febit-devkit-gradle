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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyValueImplTest {

    @Test
    void createsWithGetters() {
        var kv = KeyValueImpl.of("k", 1);
        assertEquals("k", kv.getKey());
        assertEquals(1, kv.getValue());
    }

    @Test
    void equalsAndHashCode() {
        assertEquals(KeyValueImpl.of("a", 1), KeyValueImpl.of("a", 1));
        assertEquals(KeyValueImpl.of("a", 1).hashCode(), KeyValueImpl.of("a", 1).hashCode());
        assertNotEquals(KeyValueImpl.of("a", 1), KeyValueImpl.of("a", 2));
        assertNotEquals(KeyValueImpl.of("a", 1), KeyValueImpl.of("b", 1));
    }

    @Test
    void toStringIncludesFields() {
        var s = KeyValueImpl.of("k", "v").toString();
        assertTrue(s.contains("k") && s.contains("v"));
    }
}
