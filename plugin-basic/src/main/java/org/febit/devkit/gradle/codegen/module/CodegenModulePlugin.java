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
package org.febit.devkit.gradle.codegen.module;

import org.febit.devkit.gradle.util.FolderUtils;
import org.febit.devkit.gradle.util.GradleUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;

public class CodegenModulePlugin implements Plugin<Project> {

    @Override
    public void apply(Project parent) {
        parent.allprojects(project -> {
            project.getExtensions()
                    .create(Constants.EXTENSION, CodegenModuleExtension.class, project);
            project.afterEvaluate(this::afterEvaluate);
        });
    }

    private void afterEvaluate(Project project) {
        var extension = project.getExtensions()
                .getByType(CodegenModuleExtension.class);

        if (extension.getModules().isEmpty()) {
            return;
        }

        if (!project.getPlugins().hasPlugin(JavaBasePlugin.class)) {
            throw new GradleException("Cannot init codegen-module, not a Java project.");
        }

        var main = GradleUtils.mainSourceSet(project);
        var srcDir = extension.getGeneratedSourceDir();
        var resourceDir = extension.getGeneratedResourceDir();
        FolderUtils.mkdirs(srcDir);

        main.getJava().srcDir(srcDir);
        main.getResources().srcDir(resourceDir);

        var tasks = project.getTasks();
        tasks.register(Constants.TASK_CODEGEN, CodegenModuleTask.class);

        tasks.named(JavaPlugin.COMPILE_JAVA_TASK_NAME, task -> {
            task.dependsOn(Constants.TASK_CODEGEN);
        });
    }

}
