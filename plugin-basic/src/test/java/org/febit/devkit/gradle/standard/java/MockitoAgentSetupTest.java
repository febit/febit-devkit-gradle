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

import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockitoAgentSetupTest {

    @Test
    void addsMockitoCoreFromTestRuntimeClasspath(@TempDir File projectDir) {
        var project = createProject(projectDir);
        addMockito(project, "5.24.0");

        MockitoAgentSetup.of(project).setup();

        var agents = javaAgents(project);
        assertEquals(1, agents.size(), "actual: " + agents);
        assertEquals("-javaagent:" + mockitoCoreJar(project).getAbsolutePath(),
                agents.get(0));
        assertFalse(agents.get(0).contains("byte-buddy"), "actual: " + agents);
    }

    @Test
    void agentVersionFollowsTestRuntimeClasspath(@TempDir File projectDir) {
        var project = createProject(projectDir);
        addMockito(project, "5.23.0");

        MockitoAgentSetup.of(project).setup();

        var agents = javaAgents(project);
        assertEquals(1, agents.size(), "actual: " + agents);
        assertEquals("-javaagent:" + mockitoCoreJar(project).getAbsolutePath(),
                agents.get(0));
        assertTrue(agents.get(0).endsWith("mockito-core-5.23.0.jar"), "actual: " + agents);
    }

    @Test
    void addsNoAgentWithoutMockito(@TempDir File projectDir) {
        var project = createProject(projectDir);

        MockitoAgentSetup.of(project).setup();

        assertTrue(javaAgents(project).isEmpty());
    }

    @Test
    void configuresAgentWhenJavaAppliedLater(@TempDir File projectDir) {
        var project = createProject(projectDir, false);

        // must not fail when applied before the java plugin
        MockitoAgentSetup.of(project).setup();
        project.getPlugins().apply(JavaPlugin.class);
        addMockito(project, "5.24.0");

        var agents = javaAgents(project);
        assertEquals(1, agents.size(), "actual: " + agents);
        assertEquals("-javaagent:" + mockitoCoreJar(project).getAbsolutePath(),
                agents.get(0));
    }

    @Test
    void addsAgentProviderToAnyTestTask(@TempDir File projectDir) {
        var project = createProject(projectDir);
        project.getTasks().register("anotherTest",
                org.gradle.api.tasks.testing.Test.class, task -> {
                });

        MockitoAgentSetup.of(project).setup();

        var anotherTest = (org.gradle.api.tasks.testing.Test)
                project.getTasks().getByName("anotherTest");
        assertFalse(anotherTest.getJvmArgumentProviders().isEmpty());
    }

    private static void addMockito(ProjectInternal project, String version) {
        project.getDependencies().add(
                "testImplementation", "org.mockito:mockito-junit-jupiter:" + version);
    }

    private static List<String> javaAgents(ProjectInternal project) {
        var test = (org.gradle.api.tasks.testing.Test) project.getTasks().getByName("test");
        return test.getAllJvmArgs().stream()
                .filter(arg -> arg.startsWith("-javaagent:"))
                .toList();
    }

    private static File mockitoCoreJar(ProjectInternal project) {
        return project.getConfigurations()
                .getByName(JavaPlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME)
                .getFiles()
                .stream()
                .filter(file -> file.getName().startsWith("mockito-core-"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("mockito-core is not on the test runtime classpath"));
    }

    private static ProjectInternal createProject(File projectDir) {
        return createProject(projectDir, true);
    }

    private static ProjectInternal createProject(File projectDir, boolean withJavaPlugin) {
        var project = (ProjectInternal) ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .withName("demo")
                .build();
        project.getRepositories().mavenCentral();
        if (withJavaPlugin) {
            project.getPlugins().apply(JavaPlugin.class);
        }
        return project;
    }
}
