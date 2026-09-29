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
import org.gradle.api.credentials.HttpHeaderCredentials;
import org.gradle.api.internal.TaskInternal;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlatformPlugin;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.plugins.signing.Sign;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicationSetupTest {

    private static final String GROUP = "org.febit.tests";
    private static final String RELEASES = "https://repo.example.com/releases";
    private static final String SNAPSHOTS = "https://repo.example.com/snapshots";

    @Nested
    class Publication {

        @Test
        void createsPublicationRepositoryAndInstallTask(@TempDir File projectDir) {
            var project = libraryProject(projectDir, "demo", "1.0.0");

            PublicationSetup.of(project, Map.of("releasesUrl", RELEASES)).setup();
            project.evaluate();

            assertTrue(project.getPlugins().hasPlugin(MavenPublishPlugin.class));
            assertTrue(project.getPlugins().hasPlugin("signing"));

            var publishing = project.getExtensions().getByType(PublishingExtension.class);
            assertTrue(publishing.getPublications().getByName("mavenArtifact")
                    instanceof MavenPublication);

            var install = project.getTasks().findByName("install");
            assertNotNull(install);
            assertEquals("publishing", install.getGroup());
        }

        @Test
        void packagesBomProjectAsPom(@TempDir File projectDir) throws IOException {
            var project = (ProjectInternal) ProjectBuilder.builder()
                    .withProjectDir(projectDir)
                    .withName("demo-bom")
                    .build();
            project.setGroup(GROUP);
            project.setVersion("1.0.0");
            project.getPlugins().apply(JavaPlatformPlugin.class);
            registerExtension(project);

            PublicationSetup.of(project, Map.of("releasesUrl", RELEASES)).setup();
            project.evaluate();

            assertTrue(generatePom(project).contains("<packaging>pom</packaging>"));
        }
    }

    @Nested
    class Repository {

        @Test
        void usesReleasesUrlByDefault(@TempDir File projectDir) {
            var project = libraryProject(projectDir, "demo", "1.0.0");

            PublicationSetup.of(project, Map.of("releasesUrl", RELEASES)).setup();
            project.evaluate();

            assertEquals(RELEASES, singleRepository(project).getUrl().toString());
        }

        @Test
        void fallsBackToUrlWhenNoReleasesUrl(@TempDir File projectDir) {
            var project = libraryProject(projectDir, "demo", "1.0.0");

            PublicationSetup.of(project, Map.of("url", RELEASES)).setup();
            project.evaluate();

            assertEquals(RELEASES, singleRepository(project).getUrl().toString());
        }

        @Test
        void usesSnapshotsUrlForSnapshotVersion(@TempDir File projectDir) {
            var project = libraryProject(projectDir, "demo", "1.0-SNAPSHOT");

            PublicationSetup.of(project, Map.of(
                    "url", RELEASES,
                    "snapshotsUrl", SNAPSHOTS
            )).setup();
            project.evaluate();

            assertEquals(SNAPSHOTS, singleRepository(project).getUrl().toString());
        }

        @Test
        void configuresHeaderTokenCredentials(@TempDir File projectDir) {
            var project = libraryProject(projectDir, "demo", "1.0.0");

            PublicationSetup.of(project, Map.of(
                    "releasesUrl", RELEASES,
                    "auth-header-token", "secret-token"
            )).setup();
            project.evaluate();

            var credentials = singleRepository(project)
                    .getCredentials(HttpHeaderCredentials.class);
            assertEquals("Authorization", credentials.getName());
            assertEquals("secret-token", credentials.getValue());
        }

        @Test
        void configuresUsernamePasswordCredentials(@TempDir File projectDir) {
            var project = libraryProject(projectDir, "demo", "1.0.0");

            PublicationSetup.of(project, Map.of(
                    "releasesUrl", RELEASES,
                    "username", "user",
                    "password", "pass"
            )).setup();
            project.evaluate();

            var credentials = singleRepository(project).getCredentials();
            assertEquals("user", credentials.getUsername());
            assertEquals("pass", credentials.getPassword());
        }
    }

    @Nested
    class Signing {

        @Test
        void disablesSignTasksWhenSigningNotEnabled(@TempDir File projectDir) {
            var project = libraryProject(projectDir, "demo", "1.0.0");

            PublicationSetup.of(project, Map.of("releasesUrl", RELEASES)).setup();
            project.evaluate();

            var signTasks = project.getTasks().withType(Sign.class);
            assertFalse(signTasks.isEmpty());
            signTasks.forEach(task -> assertFalse(task.isEnabled()));
        }

        @Test
        void keepsSignTasksWhenSigningEnabled(@TempDir File projectDir) {
            var project = libraryProject(projectDir, "demo", "1.0.0");

            PublicationSetup.of(project, Map.of(
                    "releasesUrl", RELEASES,
                    "signing", "true"
            )).setup();
            project.evaluate();

            var signTasks = project.getTasks().withType(Sign.class);
            assertFalse(signTasks.isEmpty());
            signTasks.forEach(task -> assertTrue(task.isEnabled()));
        }
    }

    private static ProjectInternal libraryProject(File projectDir, String name, String version) {
        var project = (ProjectInternal) ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .withName(name)
                .build();
        project.setGroup(GROUP);
        project.setVersion(version);
        project.getPlugins().apply(JavaLibraryPlugin.class);
        registerExtension(project);
        return project;
    }

    private static void registerExtension(ProjectInternal project) {
        project.getExtensions().create(
                Constants.EXTENSION,
                StandardMavenPublishExtension.class,
                "",
                Map.of()
        );
    }

    private static MavenArtifactRepository singleRepository(ProjectInternal project) {
        var repositories = project.getExtensions()
                .getByType(PublishingExtension.class)
                .getRepositories();
        assertEquals(1, repositories.size());
        return (MavenArtifactRepository) repositories.iterator().next();
    }

    private static String generatePom(ProjectInternal project) throws IOException {
        var task = (TaskInternal) project.getTasks()
                .getByName("generatePomFileForMavenArtifactPublication");
        task.getTaskActions().forEach(action -> action.execute(task));

        var buildDir = project.getLayout().getBuildDirectory().getAsFile().get();
        var pomFile = new File(buildDir, "publications/mavenArtifact/pom-default.xml");
        assertTrue(pomFile.exists());
        return FileUtils.readFileToString(pomFile, StandardCharsets.UTF_8);
    }
}
