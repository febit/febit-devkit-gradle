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

import io.freefair.gradle.plugins.lombok.LombokPlugin;
import io.freefair.gradle.plugins.lombok.tasks.Delombok;
import io.spring.gradle.dependencymanagement.DependencyManagementPlugin;
import lombok.RequiredArgsConstructor;
import org.febit.devkit.gradle.util.GitUtils;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.devkit.gradle.util.RunOnce;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.VerificationTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.Test;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;

import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RequiredArgsConstructor(staticName = "of")
class StandardJavaRegister {

    private static final String UTF_8 = "UTF-8";
    private static final String SOURCES = "sources";

    private static final String GROUP_BUILD = "build";

    private static final String TASK_SOURCE_JAR = "sourcesJar";
    private static final String TASK_DELOMBOK = "delombok";

    private final Project project;
    private final RunOnce applyOnce = RunOnce.of(this::apply);

    void register() {
        project.afterEvaluate(p -> afterProjectEvaluate());

        GradleUtils.afterPlugin(project.getPlugins(), JavaBasePlugin.class, applyOnce::runIfNot);
    }

    private void apply() {
        applySubPlugins();
        applySourceJarTask();
    }

    private void afterProjectEvaluate() {
        applyOnce.ifRan(() -> {
            configJavaTasks();
            configJarManifest();
        });
    }

    private void applySubPlugins() {
        var plugins = project.getPlugins();

        plugins.apply(JacocoPlugin.class);
        plugins.apply(LombokPlugin.class);
        plugins.apply(DependencyManagementPlugin.class);
    }

    private void configJarManifest() {
        var task = project.getTasks().findByName(JavaPlugin.JAR_TASK_NAME);
        if (!(task instanceof Jar)) {
            return;
        }
        var jar = (Jar) task;
        var buildTime = Instant.ofEpochSecond(
                System.currentTimeMillis() / 1000
        );
        var commitId = GitUtils.resolveHeadCommitId(new File(project.getRootDir(), ".git"));
        var javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);

        var attrs = new TreeMap<>(Map.of(
                "Build-Jdk", System.getProperty("java.version"),
                "Build-Jdk-Spec", javaExtension.getTargetCompatibility().toString(),
                "Build-Revision", commitId,
                "Build-Time", buildTime.toString(),
                "Created-By", "Gradle " + project.getGradle().getGradleVersion()
        ));

        attrs.putAll(Map.of(
                "Implementation-Title", project.getName(),
                "Implementation-Vendor-Id", project.getGroup().toString(),
                "Implementation-Version", project.getVersion().toString()
        ));

        jar.getManifest().attributes(attrs);
    }

    private void configJavaTasks() {
        var tasks = project.getTasks();

        tasks.withType(JavaCompile.class, task -> {
            var options = task.getOptions();
            options.setEncoding(UTF_8);
            options.getCompilerArgs().addAll(List.of(
                    "-parameters",
                    "-Xlint:unchecked",
                    "-Xlint:deprecation"
            ));
        });

        tasks.withType(Javadoc.class, javadoc -> {
            var options = javadoc.getOptions();
            options.setEncoding(UTF_8);
            if (options instanceof CoreJavadocOptions) {
                ((CoreJavadocOptions) options)
                        .addStringOption("Xdoclint:none", "-quiet");
            }
        });

        tasks.named(JavaPlugin.TEST_TASK_NAME, Test.class, task -> {
            task.getTestLogging()
                    .events("FAILED", "PASSED", "SKIPPED",
                            "STANDARD_OUT", "STANDARD_ERROR");
        });

        tasks.stream()
                .filter(t -> t instanceof VerificationTask)
                .forEach(task -> {
                    if (task.getGroup() == null) {
                        task.setGroup(JavaBasePlugin.VERIFICATION_GROUP);
                    }
                });
    }

    private void applySourceJarTask() {
        var tasks = project.getTasks();

        tasks.register(TASK_SOURCE_JAR, Jar.class, task -> {
            task.dependsOn(TASK_DELOMBOK);
            task.setGroup(GROUP_BUILD);
            task.getArchiveClassifier().set(SOURCES);

            var taskDeLombok = (Delombok) tasks.getByPath(TASK_DELOMBOK);
            task.from(
                    GradleUtils.mainSourceSet(project).getResources(),
                    taskDeLombok.getTarget()
            );
        });
    }

}
