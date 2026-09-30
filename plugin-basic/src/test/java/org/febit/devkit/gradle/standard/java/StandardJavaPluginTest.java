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

import com.diffplug.gradle.spotless.SpotlessPlugin;
import io.freefair.gradle.plugins.lombok.LombokPlugin;
import io.spring.gradle.dependencymanagement.DependencyManagementPlugin;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.logging.TestLogEvent;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.tasks.JacocoReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardJavaPluginTest {

    @Test
    void configuresStandardJavaProject(@TempDir File projectDir) {
        var project = (ProjectInternal) ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .withName("standard-java-test")
                .build();
        project.setGroup("org.febit.tests");
        project.setVersion("1.2.3");

        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(StandardJavaPlugin.class);
        project.evaluate();

        assertTrue(project.getPlugins().hasPlugin(JacocoPlugin.class));
        assertTrue(((JacocoReport) project.getTasks().getByName("jacocoTestReport"))
                .getReports().getXml().getRequired().get());
        assertTrue(project.getPlugins().hasPlugin(DependencyManagementPlugin.class));
        assertTrue(project.getPlugins().hasPlugin(LombokPlugin.class));
        assertTrue(project.getPlugins().hasPlugin(SpotlessPlugin.class));

        assertTrue(project.getTasks().getNames().contains("spotlessJavaCheck"));
        assertTrue(project.getTasks().getNames().contains("spotlessJavaApply"));
        assertEquals("verification", project.getTasks().getByName("spotlessJava").getGroup());
        assertTrue(project.getTasks().getByName("check").getDependsOn().stream()
                .anyMatch(dep -> String.valueOf(dep).contains("spotlessCheck")));
        assertTrue(project.getTasks().getNames().contains("integrationTest"));

        var compileJava = (JavaCompile) project.getTasks().getByName("compileJava");
        assertEquals("UTF-8", compileJava.getOptions().getEncoding());
        assertTrue(compileJava.getOptions().getCompilerArgs()
                .containsAll(List.of("-parameters", "-Xlint:unchecked", "-Xlint:deprecation")));

        var testTask = (org.gradle.api.tasks.testing.Test) project.getTasks().getByName("test");
        assertTrue(testTask.getTestLogging().getEvents().containsAll(Set.of(
                TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED,
                TestLogEvent.STANDARD_OUT, TestLogEvent.STANDARD_ERROR)));
        assertTrue(testTask.getIncludes().contains("**/*Test.class"));
        assertFalse(testTask.getJvmArgumentProviders().isEmpty());

        var javadoc = (Javadoc) project.getTasks().getByName("javadoc");
        assertEquals("UTF-8", javadoc.getOptions().getEncoding());

        var attrs = ((Jar) project.getTasks().getByName("jar"))
                .getManifest().getAttributes();
        assertEquals("standard-java-test", attrs.get("Implementation-Title"));
        assertEquals("org.febit.tests", attrs.get("Implementation-Vendor-Id"));
        assertEquals("1.2.3", attrs.get("Implementation-Version"));
        assertNotNull(attrs.get("Build-Revision"));
        assertNotNull(attrs.get("Build-Time"));
        assertTrue(String.valueOf(attrs.get("Created-By")).startsWith("Gradle "));
    }
}
