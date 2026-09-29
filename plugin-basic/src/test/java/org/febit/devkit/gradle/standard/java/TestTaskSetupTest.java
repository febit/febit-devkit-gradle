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

import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.testing.junitplatform.JUnitPlatformOptions;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.febit.devkit.gradle.util.GradleUtils;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestTaskSetupTest {

    private static final String PROP_JAVA_IO_TMPDIR = "-Djava.io.tmpdir=";

    @Test
    void configuresTestTask(@TempDir File projectDir) {
        var project = createProject(projectDir);
        TestTaskSetup.of(project).setup();
        project.evaluate();

        var test = test(project, "test");
        assertTrue(test.getIncludes().contains("**/*Test.class"));
        assertTrue(junitOptions(test).getExcludeTags().contains("integration"));
        assertEquals(expectedTmpDir(project), javaIoTmpDir(test));
    }

    @Test
    void configuresTestTaskWhenJavaAppliedLater(@TempDir File projectDir) {
        var project = (ProjectInternal) ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .withName("demo")
                .build();

        TestTaskSetup.of(project).setup();
        project.getPlugins().apply(JavaPlugin.class);
        project.evaluate();

        var test = test(project, "test");
        assertTrue(test.getIncludes().contains("**/*Test.class"));
        assertTrue(junitOptions(test).getExcludeTags().contains("integration"));
    }

    @Test
    void appliesTmpDirToAnyTestTask(@TempDir File projectDir) {
        var project = createProject(projectDir);
        project.getTasks().register("anotherTest",
                org.gradle.api.tasks.testing.Test.class, task -> {
                });

        TestTaskSetup.of(project).setup();

        assertEquals(expectedTmpDir(project), javaIoTmpDir(test(project, "anotherTest")));
    }

    @Test
    void registersIntegrationTestTask(@TempDir File projectDir) {
        var project = createProject(projectDir);
        TestTaskSetup.of(project).setup();

        var integrationTest = test(project, "integrationTest");
        assertEquals("Runs integration tests (JUnit tag \"integration\").",
                integrationTest.getDescription());
        assertTrue(junitOptions(integrationTest).getIncludeTags().contains("integration"));

        assertTrue(integrationTest.getShouldRunAfter()
                .getDependencies(integrationTest)
                .stream()
                .anyMatch(task -> "test".equals(task.getName())));

        var testSourceSet = GradleUtils.sourceSets(project)
                .getByName(SourceSet.TEST_SOURCE_SET_NAME);
        assertEquals(testSourceSet.getOutput().getClassesDirs().getFiles(),
                integrationTest.getTestClassesDirs().getFiles());
        assertEquals(testSourceSet.getRuntimeClasspath().getFiles(),
                integrationTest.getClasspath().getFiles());

        // only the plain "test" task filters by class name pattern
        assertTrue(integrationTest.getIncludes().isEmpty());
        assertEquals(expectedTmpDir(project), javaIoTmpDir(integrationTest));
    }

    private static org.gradle.api.tasks.testing.Test test(Project project, String name) {
        return (org.gradle.api.tasks.testing.Test) project.getTasks().getByName(name);
    }

    private static JUnitPlatformOptions junitOptions(org.gradle.api.tasks.testing.Test test) {
        return (JUnitPlatformOptions) test.getOptions();
    }

    private static String javaIoTmpDir(org.gradle.api.tasks.testing.Test test) {
        return test.getAllJvmArgs().stream()
                .filter(arg -> arg.startsWith(PROP_JAVA_IO_TMPDIR))
                .map(arg -> arg.substring(PROP_JAVA_IO_TMPDIR.length()))
                .findFirst()
                .orElse(null);
    }

    private static String expectedTmpDir(Project project) {
        return project.getLayout()
                .getBuildDirectory()
                .dir("tmp/tests")
                .get()
                .getAsFile()
                .getAbsolutePath();
    }

    private static ProjectInternal createProject(File projectDir) {
        var project = (ProjectInternal) ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .withName("demo")
                .build();
        project.getPlugins().apply(JavaPlugin.class);
        return project;
    }
}
