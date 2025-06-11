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
package org.febit.devkit.gradle.codegen.manifest;

import lombok.RequiredArgsConstructor;
import org.febit.devkit.gradle.util.GradleUtils;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.jvm.tasks.Jar;

@RequiredArgsConstructor(staticName = "of")
class CodegenManifestRegister {

    static final String EXTENSION = "codegenManifest";
    static final String GROUP = "codegen";
    static final String TASK_GENERATE_MANIFEST = "generateManifests";

    private final Project project;

    void register() {
        var extension = project.getExtensions()
                .create(EXTENSION, CodegenManifestExtension.class, project);

        var tasks = project.getTasks();
        tasks.register(TASK_GENERATE_MANIFEST, CodegenManifestTask.class, task -> {
            task.setGroup(GROUP);
            task.setDescription("Generate manifest resources.");
            task.getGeneratedResourceDir().convention(extension.getGeneratedResourceDir());
            task.getManifestStubs().convention(extension.getManifestStubs());
        });

        project.afterEvaluate(p -> afterProjectEvaluate());
    }

    private void afterProjectEvaluate() {
        var extension = project.getExtensions()
                .getByType(CodegenManifestExtension.class);

        if (!project.getPlugins().hasPlugin(JavaBasePlugin.class)) {
            return;
        }

        var main = GradleUtils.mainSourceSet(project);
        main.getResources().srcDir(extension.getGeneratedResourceDir());

        var tasks = project.getTasks();
        tasks.named(JavaPlugin.PROCESS_RESOURCES_TASK_NAME, task -> {
            task.dependsOn(TASK_GENERATE_MANIFEST);
        });
        tasks.withType(Jar.class).configureEach(
                jar -> jar.mustRunAfter(TASK_GENERATE_MANIFEST)
        );
    }

}
