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

import groovy.util.Node;
import lombok.RequiredArgsConstructor;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.devkit.gradle.util.RunOnce;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPom;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

import java.util.List;

@RequiredArgsConstructor(staticName = "of")
class ImportBomRegister {

    private static final String N_DEPENDENCY_MANAGEMENT = "dependencyManagement";
    private static final String N_DEPENDENCIES = "dependencies";
    private static final String N_DEPENDENCY = "dependency";
    private static final String N_GROUP_ID = "groupId";
    private static final String N_ARTIFACT_ID = "artifactId";
    private static final String N_VERSION = "version";
    private static final String N_SCOPE = "scope";
    private static final String N_TYPE = "type";

    private final Project project;
    private final RunOnce applyOnce = RunOnce.of(this::apply);

    void register() {
        project.afterEvaluate(p -> afterProjectEvaluate());
        GradleUtils.afterPlugin(project.getPlugins(), MavenPublishPlugin.class, applyOnce::runIfNot);
    }

    private void apply() {
    }

    private void afterProjectEvaluate() {
        applyOnce.ifRan(() -> {
            var extension = project.getExtensions()
                    .getByType(StandardMavenPublishExtension.class);

            if (extension.importBomProjects.isEmpty()) {
                return;
            }

            project.getExtensions()
                    .getByType(PublishingExtension.class)
                    .getPublications()
                    .withType(MavenPublication.class, maven -> {
                        importBom(maven.getPom(), extension.importBomProjects);
                    });
        });
    }

    private void importBom(MavenPom pom, List<Project> projects) {
        pom.withXml(xml -> {
            var dependencies = ensureChild(
                    ensureChild(xml.asNode(), N_DEPENDENCY_MANAGEMENT),
                    N_DEPENDENCIES
            );
            projects.forEach(proj -> {
                var n = dependencies.appendNode(N_DEPENDENCY);
                n.appendNode(N_GROUP_ID, proj.getGroup());
                n.appendNode(N_ARTIFACT_ID, proj.getName());
                n.appendNode(N_VERSION, proj.getVersion());
                n.appendNode(N_SCOPE, "import");
                n.appendNode(N_TYPE, "pom");
            });
        });
    }

    private Node ensureChild(Node parent, String name) {
        for (var child : parent.children()) {
            if ((child instanceof Node)
                    && ((Node) child).name().equals(name)) {
                return (Node) child;
            }
        }
        return parent.appendNode(name);
    }
}
