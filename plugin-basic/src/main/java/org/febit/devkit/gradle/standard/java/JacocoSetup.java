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

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.tasks.JacocoReport;

import org.febit.devkit.gradle.plugin.Setup;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.devkit.gradle.util.RunOnce;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class JacocoSetup implements Setup {

    private final Project project;
    private final RunOnce applyOnce = RunOnce.of(this::apply);

    @Override
    public void setup() {
        GradleUtils.afterPlugin(project.getPlugins(), JavaBasePlugin.class, applyOnce::runIfNot);
    }

    private void apply() {
        project.getPlugins().apply(JacocoPlugin.class);
        configReports();
    }

    private void configReports() {
        project.getTasks()
                .withType(JacocoReport.class)
                .configureEach(report ->
                        report.getReports().getXml().getRequired().set(true)
                );
    }
}
