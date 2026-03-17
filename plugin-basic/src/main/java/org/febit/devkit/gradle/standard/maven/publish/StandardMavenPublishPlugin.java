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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class StandardMavenPublishPlugin implements Plugin<Project> {

    @Override
    public void apply(Project parent) {

        var profileRaw = parent.findProperty("publish-profile");
        var profile = profileRaw == null ? null : profileRaw.toString();
        var config = extractConfig(profile, parent);

        parent.allprojects(project -> {
            var extension = project.getExtensions().create(
                    Constants.EXTENSION,
                    StandardMavenPublishExtension.class,
                    profile, config
            );
            if (!extension.isEnabled()) {
                return;
            }

            BasicSetup.of(project).setup();
            PublicationSetup.of(project, config).setup();
            ImportBomSetup.of(project).setup();
        });
    }

    private Map<String, String> extractConfig(@Nullable String profile, Project project) {
        if (profile == null) {
            return Map.of();
        }

        var prefix = "publish." + profile + ".";
        var map = new HashMap<String, String>();

        project.getProperties().forEach((key, value) -> {
            if (!key.startsWith(prefix) || value == null) {
                return;
            }
            map.put(key.substring(prefix.length()), value.toString());
        });
        return Map.copyOf(map);
    }

}
