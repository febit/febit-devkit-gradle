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

import com.diffplug.gradle.spotless.JavaExtension;
import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.PluginContainer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class SpotlessSetupTest {

    @Test
    @SuppressWarnings("unchecked")
    void appliesPluginAndConfiguresJavaFormat() {
        var project = mock(Project.class);
        var plugins = mock(PluginContainer.class);
        var extensions = mock(ExtensionContainer.class);
        var spotless = mock(SpotlessExtension.class);

        when(project.getPlugins()).thenReturn(plugins);
        when(project.getExtensions()).thenReturn(extensions);
        when(plugins.hasPlugin(SpotlessPlugin.class)).thenReturn(true);
        when(extensions.getByType(SpotlessExtension.class)).thenReturn(spotless);

        SpotlessSetup.of(project).setup();

        verify(plugins).apply(SpotlessPlugin.class);

        var captor = ArgumentCaptor.forClass(Action.class);
        verify(spotless).java(captor.capture());

        var java = mock(JavaExtension.class);
        captor.getValue().execute(java);

        verify(java).target("src/*/java/**/*.java");
        verify(java).setEncoding(StandardCharsets.UTF_8);
        verify(java).endWithNewline();
        verify(java).trimTrailingWhitespace();
        verifyNoMoreInteractions(java);
    }
}
