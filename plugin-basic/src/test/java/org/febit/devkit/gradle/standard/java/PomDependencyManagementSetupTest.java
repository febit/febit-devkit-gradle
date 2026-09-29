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

import io.spring.gradle.dependencymanagement.DependencyManagementPlugin;
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension;
import io.spring.gradle.dependencymanagement.dsl.GeneratedPomCustomizationHandler;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.PluginContainer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PomDependencyManagementSetupTest {

    @Test
    void enablesCustomizationForDependenciesProject() {
        assertCustomizationEnabled("foo-dependencies", true);
    }

    @Test
    void disablesCustomizationForOtherProject() {
        assertCustomizationEnabled("foo-core", false);
    }

    @SuppressWarnings("unchecked")
    private static void assertCustomizationEnabled(String projectName, boolean expected) {
        var project = mock(Project.class);
        var plugins = mock(PluginContainer.class);
        var extensions = mock(ExtensionContainer.class);
        var ext = mock(DependencyManagementExtension.class);

        when(project.getName()).thenReturn(projectName);
        when(project.getPlugins()).thenReturn(plugins);
        when(project.getExtensions()).thenReturn(extensions);
        when(plugins.hasPlugin(DependencyManagementPlugin.class)).thenReturn(true);
        when(extensions.getByType(DependencyManagementExtension.class)).thenReturn(ext);

        PomDependencyManagementSetup.of(project).setup();

        var captor = ArgumentCaptor.forClass(Action.class);
        verify(ext).generatedPomCustomization(captor.capture());

        var handler = mock(GeneratedPomCustomizationHandler.class);
        captor.getValue().execute(handler);
        verify(handler).enabled(expected);
    }
}
