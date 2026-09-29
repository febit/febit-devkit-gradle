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

import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.publish.tasks.GenerateModuleMetadata;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicSetupTest {

    @Test
    void appliesMavenPublishAndDisablesModuleMetadata(@TempDir File projectDir) {
        var project = (ProjectInternal) ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .withName("demo")
                .build();
        project.setGroup("org.febit.tests");
        project.setVersion("1.0.0");
        project.getPlugins().apply(JavaPlugin.class);

        BasicSetup.of(project).setup();

        project.getExtensions().getByType(PublishingExtension.class)
                .getPublications()
                .create("mavenArtifact", MavenPublication.class, pub ->
                        pub.from(project.getComponents().getByName("java")));

        project.evaluate();

        assertTrue(project.getPlugins().hasPlugin(MavenPublishPlugin.class));

        var metadataTasks = project.getTasks().withType(GenerateModuleMetadata.class);
        assertFalse(metadataTasks.isEmpty());
        metadataTasks.forEach(task -> assertFalse(task.isEnabled()));
    }
}
