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

import lombok.RequiredArgsConstructor;
import org.febit.devkit.gradle.util.GradleUtils;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.jvm.tasks.Jar;

@RequiredArgsConstructor(staticName = "of")
class CodegenModuleRegister {

    static final String EXTENSION = "codegenModule";
    static final String GROUP = "codegen";
    static final String TASK_GENERATE_MODULE = "generateModule";

    private final Project project;

    void register() {
        var extension = project.getExtensions()
                .create(EXTENSION, CodegenModuleExtension.class, project);

        var tasks = project.getTasks();
        tasks.register(TASK_GENERATE_MODULE, CodegenModuleTask.class, task -> {
            task.setGroup(GROUP);
            task.setDescription("Generate module files.");

            task.getGroupId().convention(project.provider(() -> project.getGroup().toString()));
            task.getArtifactId().convention(project.provider(project::getName));
            task.getVersion().convention(project.provider(() -> project.getVersion().toString()));
            task.getGeneratedSourceDir().convention(extension.getGeneratedSourceDir());
            task.getGitDir().convention(extension.getGitDir());
            task.getModules().convention(extension.getModules());
        });

        project.afterEvaluate(p -> afterProjectEvaluate());
    }

    private void afterProjectEvaluate() {
        var extension = project.getExtensions()
                .getByType(CodegenModuleExtension.class);

        if (!project.getPlugins().hasPlugin(JavaBasePlugin.class)) {
            return;
        }

        var main = GradleUtils.mainSourceSet(project);
        main.getJava().srcDir(extension.getGeneratedSourceDir());
        main.getResources().srcDir(extension.getGeneratedResourceDir());

        var tasks = project.getTasks();
        tasks.named(JavaPlugin.COMPILE_JAVA_TASK_NAME, task -> {
            task.dependsOn(TASK_GENERATE_MODULE);
        });
        tasks.withType(Jar.class).configureEach(
                jar -> jar.mustRunAfter(TASK_GENERATE_MODULE)
        );
    }

}
