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
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.testing.Test;

import org.febit.devkit.gradle.plugin.Setup;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.devkit.gradle.util.RunOnce;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class MockitoAgentSetup implements Setup {

    private static final String MOCKITO_GROUP = "org.mockito";
    private static final String MOCKITO_CORE = "mockito-core";

    private final Project project;
    private final RunOnce applyOnce = RunOnce.of(this::apply);

    @Override
    public void setup() {
        // the test runtime classpath is created by the java plugin (not by java-base)
        GradleUtils.afterPlugin(project.getPlugins(), JavaPlugin.class, applyOnce::runIfNot);
    }

    private void apply() {
        configTestTasks();
    }

    private void configTestTasks() {
        // Take mockito-core (the only jar carrying "Premain-Class") from the test runtime
        // classpath, so the agent always matches the mockito version used by the tests.
        // Empty when the project does not use mockito - then no agent is added at all.
        var mockitoCore = project.getConfigurations()
                .named(JavaPlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME)
                .map(configuration -> configuration.getIncoming()
                        .artifactView(view -> view.componentFilter(MockitoAgentSetup::isMockitoCore))
                        .getFiles());

        project.getTasks().withType(Test.class).configureEach(test ->
                test.getJvmArgumentProviders().add(mockitoAgentProvider(mockitoCore))
        );
    }

    private static boolean isMockitoCore(ComponentIdentifier id) {
        return id instanceof ModuleComponentIdentifier module
                && MOCKITO_GROUP.equals(module.getGroup())
                && MOCKITO_CORE.equals(module.getModule());
    }

    private MockitoAgentProvider mockitoAgentProvider(Provider<FileCollection> mockitoCore) {
        var provider = project.getObjects().newInstance(MockitoAgentProvider.class);
        provider.getAgentJar().from(mockitoCore);
        return provider;
    }
}
