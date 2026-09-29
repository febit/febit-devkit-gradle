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

import org.apache.commons.io.FileUtils;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardMavenPublishPluginTest {

    @Test
    void createsEnabledExtensionAndAppliesPublishPlugins() {
        var project = (ProjectInternal) ProjectBuilder.builder().build();

        project.getPlugins().apply(StandardMavenPublishPlugin.class);

        var extension = project.getExtensions().findByType(StandardMavenPublishExtension.class);
        assertNotNull(extension);
        assertTrue(extension.isEnabled());
        assertTrue(extension.getConfig().isEmpty());

        assertTrue(project.getPlugins().hasPlugin("maven-publish"));
        assertTrue(project.getPlugins().hasPlugin("signing"));
    }

    @Test
    void readsPublishProfileProperty() {
        var project = (ProjectInternal) ProjectBuilder.builder().build();
        project.getExtensions().getExtraProperties().set("publish-profile", "dev");

        project.getPlugins().apply(StandardMavenPublishPlugin.class);

        var extension = project.getExtensions().getByType(StandardMavenPublishExtension.class);
        assertEquals("dev", extension.getProfile());
    }

    @Test
    void extractsConfigFromPrefixedGradleProperties(@TempDir File projectDir) throws IOException {
        writeGradleProperties(projectDir, String.join(System.lineSeparator(),
                "publish.dev.releasesUrl=https://repo.example.com/releases",
                "publish.dev.signing=true",
                "publish.other.url=https://other.example.com"));

        var project = (ProjectInternal) ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .build();
        project.getExtensions().getExtraProperties().set("publish-profile", "dev");

        project.getPlugins().apply(StandardMavenPublishPlugin.class);

        var extension = project.getExtensions().getByType(StandardMavenPublishExtension.class);
        assertEquals("dev", extension.getProfile());
        assertEquals(Map.of(
                "releasesUrl", "https://repo.example.com/releases",
                "signing", "true"
        ), extension.getConfig());
    }

    @Test
    void ignoresPrefixedPropertiesWithoutProfile(@TempDir File projectDir) throws IOException {
        writeGradleProperties(projectDir,
                "publish.dev.releasesUrl=https://repo.example.com/releases");

        var project = (ProjectInternal) ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .build();

        project.getPlugins().apply(StandardMavenPublishPlugin.class);

        var extension = project.getExtensions().getByType(StandardMavenPublishExtension.class);
        assertTrue(extension.getConfig().isEmpty());
    }

    @Test
    void appliesConfigFromPrefixedProperties(@TempDir File projectDir) throws IOException {
        writeGradleProperties(projectDir,
                "publish.dev.releasesUrl=https://repo.example.com/releases");

        var project = (ProjectInternal) ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .withName("demo")
                .build();
        project.setGroup("org.febit.tests");
        project.setVersion("1.0.0");
        project.getExtensions().getExtraProperties().set("publish-profile", "dev");
        project.getPlugins().apply(JavaLibraryPlugin.class);

        project.getPlugins().apply(StandardMavenPublishPlugin.class);
        project.evaluate();

        var repositories = project.getExtensions()
                .getByType(PublishingExtension.class)
                .getRepositories();
        assertEquals(1, repositories.size());
        var repository = (MavenArtifactRepository) repositories.iterator().next();
        assertEquals("https://repo.example.com/releases", repository.getUrl().toString());
    }

    private static void writeGradleProperties(File projectDir, String content) throws IOException {
        FileUtils.write(new File(projectDir, "gradle.properties"),
                content + System.lineSeparator(), StandardCharsets.UTF_8);
    }
}
