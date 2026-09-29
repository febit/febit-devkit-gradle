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
package org.febit.devkit.gradle.standard.java;

import io.freefair.gradle.plugins.lombok.LombokExtension;
import io.freefair.gradle.plugins.lombok.LombokPlugin;
import org.gradle.api.DefaultTask;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import org.febit.devkit.gradle.task.CodegenTask;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LombokSetupTest {

    @Test
    void appliesPluginAndSetsDefaultVersion() {
        var project = (ProjectInternal) ProjectBuilder.builder().build();
        project.getPlugins().apply(JavaPlugin.class);

        LombokSetup.of(project).setup();
        project.evaluate();

        assertTrue(project.getPlugins().hasPlugin(LombokPlugin.class));

        // NOTE: LombokSetup pins VERSION, which currently equals the freefair
        //       plugin's own default (1.18.48), so only presence can be asserted.
        var ext = project.getExtensions().getByType(LombokExtension.class);
        assertNotNull(ext.getVersion().getOrNull());
    }

    @Test
    void ordersLombokConfigAfterCodegenTasks() {
        var project = (ProjectInternal) ProjectBuilder.builder().build();
        project.getPlugins().apply(JavaPlugin.class);

        LombokSetup.of(project).setup();
        project.getTasks().register("demoCodegen", CodegenTaskImpl.class);
        project.evaluate();

        var lombokConfig = project.getTasks().getByName("generateEffectiveLombokConfig");
        assertTrue(lombokConfig.getMustRunAfter()
                        .getDependencies(lombokConfig)
                        .stream()
                        .anyMatch(task -> "demoCodegen".equals(task.getName())),
                "actual mustRunAfter: " + lombokConfig.getMustRunAfter());
    }

    public static class CodegenTaskImpl extends DefaultTask implements CodegenTask {
    }
}
