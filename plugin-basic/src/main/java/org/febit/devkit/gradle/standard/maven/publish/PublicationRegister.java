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

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.febit.devkit.gradle.standard.util.StandardUtils;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.devkit.gradle.util.RunOnce;
import org.gradle.api.NonNullApi;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.credentials.HttpHeaderCredentials;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlatformPlugin;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.VersionMappingStrategy;
import org.gradle.api.publish.maven.MavenPom;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.authentication.http.HttpHeaderAuthentication;
import org.gradle.plugins.signing.SigningExtension;

import java.util.Map;

@NonNullApi
@RequiredArgsConstructor(staticName = "of")
public class PublicationRegister {

    private static final String RUNTIME_CLASSPATH = "runtimeClasspath";
    private static final String PUBLICATION_NAME = "mavenArtifact";

    private final Project project;

    private final Map<String, String> config;

    private final RunOnce applyOnce = RunOnce.of(this::apply);

    public void register() {
        project.afterEvaluate(p -> afterProjectEvaluate());

        val plugins = project.getPlugins();
        plugins.apply("signing");
        plugins.apply("maven-publish");

        GradleUtils.afterPlugin(plugins, MavenPublishPlugin.class, this::applyIfReady);
        GradleUtils.afterPlugin(plugins, JavaPlatformPlugin.class, this::applyIfReady);
        GradleUtils.afterPlugin(plugins, JavaLibraryPlugin.class, this::applyIfReady);
    }

    private void applyIfReady() {
        val plugins = project.getPlugins();
        if (!plugins.hasPlugin(MavenPublishPlugin.class)) {
            return;
        }
        if (!plugins.hasPlugin(JavaPlatformPlugin.class)
                && !plugins.hasPlugin(JavaLibraryPlugin.class)
        ) {
            return;
        }
        this.applyOnce.runIfNot();
    }

    private void apply() {
        configSigning();

        val publishing = project.getExtensions()
                .getByType(PublishingExtension.class);

        publishing.publications(this::configPublications);
        publishing.getRepositories().maven(this::configMaven);
    }

    private void configSigning() {
        val signing = project.getExtensions()
                .getByType(SigningExtension.class);
        val publishing = project.getExtensions()
                .getByType(PublishingExtension.class);

        signing.setRequired(
                "true".equals(config.get("signing"))
        );
        signing.sign(publishing.getPublications());
    }

    private void configMaven(MavenArtifactRepository maven) {
        if ("true".equals(config.get("allowInsecureProtocol"))) {
            maven.setAllowInsecureProtocol(true);
        }

        val url = config.getOrDefault(
                StandardUtils.isSnapshot(project)
                        ? "snapshotsUrl"
                        : "releasesUrl",
                config.get("url")
        );
        if (StringUtils.isNotEmpty(url)) {
            maven.setUrl(url);
        }

        val authHeaderToken = config.get("auth-header-token");
        if (StringUtils.isNotEmpty(authHeaderToken)) {
            maven.getAuthentication()
                    .create("header", HttpHeaderAuthentication.class);
            val credentials = maven.getCredentials(HttpHeaderCredentials.class);
            credentials.setName(
                    config.getOrDefault("auth-header", "Authorization")
            );
            credentials.setValue(authHeaderToken);
        }

        val password = config.get("password");
        if (StringUtils.isNotEmpty(password)) {
            var credentials = maven.getCredentials();
            credentials.setUsername(config.get("username"));
            credentials.setPassword(password);
        }
    }

    private void configPublications(PublicationContainer publications) {
        val components = project.getComponents();

        val platformComp = components.findByName("javaPlatform");
        val javaComp = components.findByName("java");

        if (platformComp == null && javaComp == null) {
            return;
        }

        publications.create(PUBLICATION_NAME, MavenPublication.class, pub -> {
            pub.from(platformComp != null ? platformComp : javaComp);
        });
    }

    private void afterProjectEvaluate() {
        applyOnce.ifRan(() -> {
            project.getExtensions()
                    .getByType(PublishingExtension.class)
                    .getPublications()
                    .withType(MavenPublication.class, maven -> {
                        maven.versionMapping(this::configVersionMapping);
                        maven.pom(this::pomPackaging);
                        maven.pom(this::pomActions);
                    });
        });
    }

    private void configVersionMapping(VersionMappingStrategy mapping) {
        val hasRuntimeClasspath = project.getConfigurations()
                .getNames().contains(RUNTIME_CLASSPATH);

        if (!hasRuntimeClasspath) {
            return;
        }

        mapping.usage("java-api", strategy ->
                strategy.fromResolutionOf(RUNTIME_CLASSPATH)
        );
        mapping.usage("java-runtime", strategy ->
                strategy.fromResolutionOf(RUNTIME_CLASSPATH)
        );
    }

    private void pomActions(MavenPom pom) {
        project.getExtensions()
                .getByType(StandardMavenPublishExtension.class)
                .pomActions
                .forEach(action -> action.accept(pom));
    }

    private void pomPackaging(MavenPom pom) {
        if (StandardUtils.isBom(project)
                || StandardUtils.isDependencies(project)) {
            pom.setPackaging("pom");
        }
    }

}
