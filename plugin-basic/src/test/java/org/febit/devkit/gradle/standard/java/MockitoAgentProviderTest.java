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
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockitoAgentProviderTest {

    @Test
    void providesJavaagentArgument(@TempDir File projectDir) {
        var project = ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .build();
        var agent = new File(projectDir, "mockito-agent.jar");

        var provider = provider(project);
        provider.getAgentJar().from(agent);

        assertEquals(List.of("-javaagent:" + agent.getAbsolutePath()),
                arguments(provider));
    }

    @Test
    void providesNoArgumentWhenAgentJarMissing(@TempDir File projectDir) {
        var project = ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .build();

        assertTrue(arguments(provider(project)).isEmpty());
    }

    @Test
    void providesOneArgumentPerJar(@TempDir File projectDir) {
        var project = ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .build();
        var first = new File(projectDir, "first.jar");
        var second = new File(projectDir, "second.jar");

        var provider = provider(project);
        provider.getAgentJar().from(first, second);

        assertEquals(List.of(
                "-javaagent:" + first.getAbsolutePath(),
                "-javaagent:" + second.getAbsolutePath()
        ), arguments(provider));
    }

    private static MockitoAgentProvider provider(Project project) {
        return project.getObjects().newInstance(MockitoAgentProvider.class);
    }

    private static List<String> arguments(MockitoAgentProvider provider) {
        var arguments = new ArrayList<String>();
        provider.asArguments().forEach(arguments::add);
        return arguments;
    }
}
