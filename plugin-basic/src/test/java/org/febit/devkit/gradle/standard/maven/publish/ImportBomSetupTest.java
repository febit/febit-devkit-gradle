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
import org.gradle.api.internal.TaskInternal;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportBomSetupTest {

    private static final String GROUP = "org.febit.tests";

    @Test
    void importsBomIntoPomDependencyManagement(@TempDir File projectDir) throws IOException {
        var project = createProject(projectDir);
        var extension = registerExtension(project);
        extension.importBom(createBomProject(projectDir, "platform-bom"));

        setupPublication(project);
        ImportBomSetup.of(project).setup();
        project.evaluate();

        var pom = generatePom(project);
        assertTrue(pom.contains("<dependencyManagement>"));
        assertTrue(pom.contains("<artifactId>platform-bom</artifactId>"));
        assertTrue(pom.contains("<scope>import</scope>"));
        assertTrue(pom.contains("<type>pom</type>"));
    }

    @Test
    void importsMultipleBoms(@TempDir File projectDir) throws IOException {
        var project = createProject(projectDir);
        var extension = registerExtension(project);
        extension.importBom(createBomProject(projectDir, "bom-one"));
        extension.importBom(createBomProject(projectDir, "bom-two"));

        setupPublication(project);
        ImportBomSetup.of(project).setup();
        project.evaluate();

        var pom = generatePom(project);
        assertTrue(pom.contains("<artifactId>bom-one</artifactId>"));
        assertTrue(pom.contains("<artifactId>bom-two</artifactId>"));
    }

    @Test
    void reusesExistingDependencyManagementNode(@TempDir File projectDir) throws IOException {
        var project = createProject(projectDir);
        var extension = registerExtension(project);
        extension.importBom(createBomProject(projectDir, "platform-bom"));

        var publication = setupPublication(project);
        publication.getPom().withXml(xml ->
                xml.asNode().appendNode("dependencyManagement").appendNode("dependencies"));

        ImportBomSetup.of(project).setup();
        project.evaluate();

        var pom = generatePom(project);
        assertEquals(1, countOccurrences(pom, "<dependencyManagement>"));
        assertTrue(pom.contains("<artifactId>platform-bom</artifactId>"));
    }

    @Test
    void skipsWhenNoBomConfigured(@TempDir File projectDir) throws IOException {
        var project = createProject(projectDir);
        registerExtension(project);

        setupPublication(project);
        ImportBomSetup.of(project).setup();
        project.evaluate();

        assertFalse(generatePom(project).contains("<dependencyManagement>"));
    }

    private static ProjectInternal createProject(File projectDir) {
        var project = (ProjectInternal) ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .withName("demo")
                .build();
        project.setGroup(GROUP);
        project.setVersion("1.0.0");
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(MavenPublishPlugin.class);
        return project;
    }

    private static ProjectInternal createBomProject(File projectDir, String name) {
        var project = (ProjectInternal) ProjectBuilder.builder()
                .withProjectDir(new File(projectDir, name))
                .withName(name)
                .build();
        project.setGroup(GROUP);
        project.setVersion("2.0.0");
        return project;
    }

    private static StandardMavenPublishExtension registerExtension(ProjectInternal project) {
        return project.getExtensions().create(
                Constants.EXTENSION,
                StandardMavenPublishExtension.class,
                "",
                Map.of()
        );
    }

    private static MavenPublication setupPublication(ProjectInternal project) {
        var publication = project.getExtensions().getByType(PublishingExtension.class)
                .getPublications()
                .create("mavenArtifact", MavenPublication.class, pub ->
                        pub.from(project.getComponents().getByName("java")));
        return (MavenPublication) publication;
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

    private static int countOccurrences(String text, String token) {
        int count = 0;
        int index = text.indexOf(token);
        while (index >= 0) {
            count++;
            index = text.indexOf(token, index + token.length());
        }
        return count;
    }
}
