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

import io.spring.gradle.dependencymanagement.DependencyManagementPlugin;
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension;
import lombok.RequiredArgsConstructor;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.devkit.gradle.util.RunOnce;
import org.gradle.api.NonNullApi;
import org.gradle.api.Project;

@NonNullApi
@RequiredArgsConstructor(staticName = "of")
public class StandardPomDependencyManagementRegister {

    private static final String SUFFIX_DEPENDENCIES = "-dependencies";
    private final RunOnce applyOnce = RunOnce.of(this::apply);

    private final Project project;

    public void register() {
        project.afterEvaluate(p -> afterProjectEvaluate());
        GradleUtils.afterPlugin(project.getPlugins(), DependencyManagementPlugin.class, applyOnce::runIfNot);
    }

    private void apply() {
        var ext = project.getExtensions()
                .getByType(DependencyManagementExtension.class);

        var needDepMgmt = project.getName().endsWith(SUFFIX_DEPENDENCIES);
        ext.generatedPomCustomization(h -> h.enabled(needDepMgmt));
    }

    private void afterProjectEvaluate() {
        // Nothing
    }

}
