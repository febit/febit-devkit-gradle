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
package org.febit.devkit.gradle.standard.bom;

import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencyConstraint;
import org.gradle.api.plugins.JavaPlatformPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardBomPluginTest {

    private static final String GROUP = "org.febit.tests";
    private static final String VERSION = "1.0.0";

    @Test
    void throwsWhenNameDoesNotEndWithBom(@TempDir File rootDir) {
        var project = ProjectBuilder.builder()
                .withProjectDir(rootDir)
                .withName("demo-core")
                .build();

        var plugin = new StandardBomPlugin();
        var ex = assertThrows(UnsupportedOperationException.class,
                () -> plugin.apply(project));
        assertTrue(ex.getMessage().contains("-bom"));
        assertTrue(ex.getMessage().contains("demo-core"));
    }

    @Test
    void addsConstraintsForSiblingJavaProjects(@TempDir File rootDir) {
        var root = ProjectBuilder.builder()
                .withProjectDir(rootDir)
                .withName("root")
                .build();

        createJavaModule(root, rootDir, "mod-b");
        createJavaModule(root, rootDir, "mod-a");

        var bom = ProjectBuilder.builder()
                .withParent(root)
                .withProjectDir(new File(rootDir, "demo-bom"))
                .withName("demo-bom")
                .build();

        bom.getPlugins().apply(StandardBomPlugin.class);

        assertTrue(bom.getPlugins().hasPlugin(JavaPlatformPlugin.class));

        var constraints = bom.getConfigurations()
                .getByName("api")
                .getDependencyConstraints();
        assertEquals(Set.of("mod-a", "mod-b"), constraints.stream()
                .map(DependencyConstraint::getName)
                .collect(Collectors.toSet()));
        constraints.forEach(constraint -> {
            assertEquals(GROUP, constraint.getGroup());
            assertEquals(VERSION, constraint.getVersion());
        });
    }

    private static void createJavaModule(Project parent, File rootDir, String name) {
        var module = ProjectBuilder.builder()
                .withParent(parent)
                .withProjectDir(new File(rootDir, name))
                .withName(name)
                .build();
        module.setGroup(GROUP);
        module.setVersion(VERSION);
        module.getPlugins().apply(JavaPlugin.class);
    }
}
