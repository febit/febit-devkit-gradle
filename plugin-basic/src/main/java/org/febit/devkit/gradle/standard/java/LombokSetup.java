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
import lombok.RequiredArgsConstructor;
import org.febit.devkit.gradle.plugin.Setup;
import org.febit.devkit.gradle.task.CodegenTask;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.devkit.gradle.util.RunOnce;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;

@RequiredArgsConstructor(staticName = "of")
public class LombokSetup implements Setup {

    private static final String TASK_GEN_CONFIG_MAIN = "generateEffectiveLombokConfig";
    private static final String VERSION = "1.18.46";

    private final Project project;
    private final RunOnce applyOnce = RunOnce.of(this::apply);

    @Override
    public void setup() {
        project.afterEvaluate(p -> afterProjectEvaluate());
        GradleUtils.afterPlugin(project.getPlugins(), JavaBasePlugin.class, applyOnce::runIfNot);
    }

    private void apply() {
        project.getPlugins().apply(LombokPlugin.class);
    }

    private void afterProjectEvaluate() {
        applyOnce.ifRan(() -> {
            configVersion();
            configTasks();
        });
    }

    private void configVersion() {
        var ex = project.getExtensions().getByType(LombokExtension.class);
        ex.getVersion().convention(VERSION);
    }

    private void configTasks() {
        var tasks = project.getTasks();
        var codegenTasks = tasks.withType(CodegenTask.class).toArray();
        if (codegenTasks.length == 0) {
            return;
        }
        tasks.matching(t -> TASK_GEN_CONFIG_MAIN.equals(t.getName()))
                .forEach(task -> {
                    task.mustRunAfter(codegenTasks);
                });
    }

}
