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

import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;
import org.gradle.api.Project;

import org.febit.devkit.gradle.plugin.Setup;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.devkit.gradle.util.RunOnce;

import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;

import static org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME;

@RequiredArgsConstructor(staticName = "of")
public class SpotlessSetup implements Setup {

    private static final String SPOTLESS_CHECK_TASK_NAME = "spotlessCheck";
    private static final String DEFAULT_TARGET = "src/*/java/**/*.java";

    private final Project project;
    private final RunOnce applyOnce = RunOnce.of(this::apply);

    @Override
    public void setup() {
        GradleUtils.afterPlugin(project.getPlugins(), SpotlessPlugin.class, applyOnce::runIfNot);
        project.afterEvaluate(p -> afterProjectEvaluate());
        project.getPlugins().apply(SpotlessPlugin.class);
    }

    private void apply() {
        var spotless = project.getExtensions().getByType(SpotlessExtension.class);
        spotless.java(java -> {
            java.target(DEFAULT_TARGET);
            java.setEncoding(StandardCharsets.UTF_8);
            java.endWithNewline();
            java.trimTrailingWhitespace();
        });
    }

    private void afterProjectEvaluate() {
        applyOnce.ifRan(this::configTasks);
    }

    private void configTasks() {
        var tasks = project.getTasks();
        tasks.named(CHECK_TASK_NAME)
                .configure(t ->
                        t.dependsOn(SPOTLESS_CHECK_TASK_NAME)
                );
    }

}
