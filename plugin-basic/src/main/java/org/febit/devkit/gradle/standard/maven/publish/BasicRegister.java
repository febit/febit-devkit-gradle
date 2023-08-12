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
package org.febit.devkit.gradle.standard.maven.publish;

import lombok.RequiredArgsConstructor;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.devkit.gradle.util.RunOnce;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven;
import org.gradle.api.publish.tasks.GenerateModuleMetadata;

@RequiredArgsConstructor(staticName = "of")
class BasicRegister {

    private static final String PUB_PLUGIN_MAVEN = "pluginMaven";

    private final Project project;
    private final RunOnce applyOnce = RunOnce.of(this::apply);

    void register() {
        project.afterEvaluate(p -> afterProjectEvaluate());
        GradleUtils.afterPlugin(project.getPlugins(), JavaBasePlugin.class, applyOnce::runIfNot);
    }

    private void apply() {
        var plugins = project.getPlugins();
        plugins.apply(MavenPublishPlugin.class);
    }

    private void afterProjectEvaluate() {
        applyOnce.ifRan(() -> {
            disableGradleModuleMetadataPublication();
            disablePluginMavenTasks();
        });
    }

    private void disablePluginMavenTasks() {
        var tasks = project.getTasks();
        tasks.withType(AbstractPublishToMaven.class).configureEach(task -> {
            task.onlyIf(spec -> {
                String pubName = task.getPublication().getName();
                return !PUB_PLUGIN_MAVEN.equals(pubName);
            });
        });
    }

    private void disableGradleModuleMetadataPublication() {
        // see: https://docs.gradle.org/8.0/userguide/publishing_gradle_module_metadata.html#sub:disabling-gmm-publication
        project.getTasks().withType(GenerateModuleMetadata.class, task -> {
            task.setEnabled(false);
        });
    }


}
