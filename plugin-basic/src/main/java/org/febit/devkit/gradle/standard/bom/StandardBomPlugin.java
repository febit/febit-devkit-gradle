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

import lombok.val;
import org.febit.devkit.gradle.standard.util.StandardUtils;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.devkit.gradle.util.RunOnce;
import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlatformPlugin;

import java.util.Comparator;

import static org.febit.devkit.gradle.util.Defaults.nvl;

@NonNullApi
public class StandardBomPlugin implements Plugin<Project> {

    private static final String CONF_API = "api";

    private Project project;
    private final RunOnce applyOnce = RunOnce.of(this::apply);

    @Override
    public void apply(Project project) {
        this.project = project;
        if (!StandardUtils.isBom(project)) {
            throw new UnsupportedOperationException(
                    "name of BOM module should end with '-bom', but now is '" + project.getName() + "'");
        }

        val plugins = project.getPlugins();
        plugins.apply(JavaPlatformPlugin.class);

        GradleUtils.afterPlugin(plugins, JavaPlatformPlugin.class, applyOnce::runIfNot);
    }

    private void apply() {
        val constraints = project.getDependencies().getConstraints();
        val subProjects = nvl(project.getParent(), project)
                .getSubprojects();

        subProjects.stream()
                .filter(proj -> proj != project)
                .filter(proj -> proj.getPlugins().hasPlugin(JavaBasePlugin.class))
                .sorted(Comparator.comparing(Project::getName))
                .forEach(proj ->
                        constraints.add(CONF_API, proj)
                );
    }
}
