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

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.publish.maven.MavenPom;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class StandardMavenPublishExtension {

    @Getter
    private final String profile;

    @Getter
    private final Map<String, String> config;

    @Getter
    @Setter
    private boolean enabled = true;

    final List<Consumer<MavenPom>> pomActions = new ArrayList<>();
    final List<Project> importBomProjects = new ArrayList<>();

    @Inject
    public StandardMavenPublishExtension(
            String profile,
            Map<String, String> config
    ) {
        this.profile = profile;
        this.config = config;
    }

    public void importBom(Project proj) {
        importBomProjects.add(proj);
    }

    public void pom(Action<? super MavenPom> configure) {
        pomActions.add(configure::execute);
    }
}
