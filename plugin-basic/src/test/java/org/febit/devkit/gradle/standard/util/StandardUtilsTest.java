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
package org.febit.devkit.gradle.standard.util;

import org.gradle.api.Project;
import org.tabletest.junit.TableTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StandardUtilsTest {

    @TableTest("""
            Scenario        | version      | expectSnapshot
            snapshot suffix | 1.0-SNAPSHOT | true
            release version | 1.0.0        | false
            empty version   | ''           | false
            null version    |              | false
            """)
    void isSnapshot(String version, boolean expectSnapshot) {
        var project = mock(Project.class);
        when(project.getVersion()).thenReturn(version);
        assertEquals(expectSnapshot, StandardUtils.isSnapshot(project));
    }

    @TableTest("""
            Scenario     | name      | expectBom
            bom suffix   | demo-bom  | true
            other suffix | demo-core | false
            no suffix    | bom       | false
            """)
    void isBom(String name, boolean expectBom) {
        var project = mock(Project.class);
        when(project.getName()).thenReturn(name);
        assertEquals(expectBom, StandardUtils.isBom(project));
    }

    @TableTest("""
            Scenario            | name              | expectDependencies
            dependencies suffix | demo-dependencies | true
            bom suffix          | demo-bom          | false
            no suffix           | dependencies      | false
            """)
    void isDependencies(String name, boolean expectDependencies) {
        var project = mock(Project.class);
        when(project.getName()).thenReturn(name);
        assertEquals(expectDependencies, StandardUtils.isDependencies(project));
    }
}
