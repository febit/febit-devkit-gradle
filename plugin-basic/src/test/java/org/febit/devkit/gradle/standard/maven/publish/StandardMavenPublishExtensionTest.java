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

import org.gradle.api.Project;
import org.gradle.api.publish.maven.MavenPom;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class StandardMavenPublishExtensionTest {

    @Test
    void exposesProfileConfigAndEnabled() {
        var config = Map.of("url", "https://example.com");
        var extension = new StandardMavenPublishExtension("dev", config);

        assertEquals("dev", extension.getProfile());
        assertEquals(config, extension.getConfig());
        assertTrue(extension.isEnabled());

        extension.setEnabled(false);
        assertFalse(extension.isEnabled());
    }

    @Test
    void collectsImportBomProjects() {
        var extension = new StandardMavenPublishExtension("", Map.of());
        var project = mock(Project.class);

        extension.importBom(project);

        assertEquals(List.of(project), extension.importBomProjects);
    }

    @Test
    void registersAndRunsPomActions() {
        var extension = new StandardMavenPublishExtension("", Map.of());
        var pom = mock(MavenPom.class);

        extension.pom(p -> p.setPackaging("pom"));
        assertEquals(1, extension.pomActions.size());

        extension.pomActions.get(0).accept(pom);

        verify(pom).setPackaging("pom");
    }
}
