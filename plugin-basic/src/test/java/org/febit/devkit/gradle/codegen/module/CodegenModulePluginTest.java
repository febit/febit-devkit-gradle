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
package org.febit.devkit.gradle.codegen.module;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.febit.devkit.gradle.codegen.module.CodegenModuleExtension.CODEGEN_MODULE;
import static org.febit.devkit.gradle.codegen.module.CodegenModuleExtension.febitTmpl;
import static org.junit.jupiter.api.Assertions.*;

class CodegenModulePluginTest {

    static final String TARGET_DIR = "build/generated/sources/" + CODEGEN_MODULE;
    static final String PROJ_GROUP = "org.febit.tests";
    static final String PROJ_VERSION = "1.1.0-beta.1";

    @Test
    void basic() throws IOException {
        var scene = PluginTestScene.create("basic", "basic");
        var project = scene.project();
        project.setGroup(PROJ_GROUP);
        project.setVersion(PROJ_VERSION);

        var extension = scene.extension();
        extension.module("demo.TestModule");

        scene.execute();

        var moduleJavaFile = scene.file(TARGET_DIR + "/demo/TestModule.java");
        assertTrue(moduleJavaFile.exists());

        var moduleJava = FileUtils.readFileToString(moduleJavaFile, StandardCharsets.UTF_8);
        assertTrue(moduleJava.contains("package demo;"));
        assertTrue(moduleJava.contains("public class TestModule {\n"));

        assertTrue(moduleJava.contains(
                "    public static String groupId() {\n" +
                        "        return \"" + PROJ_GROUP + "\";\n" +
                        "    }"));
        assertTrue(moduleJava.contains(
                "    public static String artifactId() {\n" +
                        "        return \"" + project.getName() + "\";\n" +
                        "    }"));
        assertTrue(moduleJava.contains(
                "    public static String version() {\n" +
                        "        return \"" + PROJ_VERSION + "\";\n" +
                        "    }"));

        assertTrue(moduleJava.contains(
                "    public static String commitId() {\n" +
                        "        return \"UNKNOWN\";\n" +
                        "    }"));

        assertTrue(moduleJava.contains("public static Instant buildTime() {\n"));
        assertTrue(moduleJava.contains("        return Instant.ofEpochSecond("));
    }

    @Test
    void withFebitTmpl() throws IOException {
        var scene = PluginTestScene.create("basic", "with-febit-tmpl");
        var project = scene.project();
        project.setGroup(PROJ_GROUP);
        project.setVersion(PROJ_VERSION);

        var extension = scene.extension();
        extension.module("demo.TestWithFebitTmpl", febitTmpl());

        var relocatedDir = scene.file("src/codegen-module").getAbsoluteFile();
        extension.getGeneratedSourceDir().set(relocatedDir);

        scene.execute();

        var moduleJavaFile = new File(relocatedDir, "demo/TestWithFebitTmpl.java");
        assertTrue(moduleJavaFile.exists());

        var moduleJava = FileUtils.readFileToString(moduleJavaFile, StandardCharsets.UTF_8);
        assertTrue(moduleJava.contains("public class TestWithFebitTmpl implements org.febit.lang.module.IModule {\n"));
    }

    @Test
    void withGitDir() throws IOException {
        var scene = PluginTestScene.create("basic", "with-git-dir");
        var project = scene.project();
        project.setGroup(PROJ_GROUP);
        project.setVersion(PROJ_VERSION);

        var extension = scene.extension();
        extension.module("demo.TestWithGitDir");

        var commitId = "1234567890abcdef1234567890abcdef12345678";
        var gitHead = scene.file(".git/HEAD");
        FileUtils.createParentDirectories(gitHead);
        FileUtils.writeStringToFile(gitHead, commitId, StandardCharsets.UTF_8);

        scene.execute();

        var moduleJavaFile = scene.file(TARGET_DIR + "/demo/TestWithGitDir.java");
        assertTrue(moduleJavaFile.exists());

        var moduleJava = FileUtils.readFileToString(moduleJavaFile, StandardCharsets.UTF_8);
        assertTrue(moduleJava.contains(
                "    public static String commitId() {\n" +
                        "        return \"" + commitId + "\";\n" +
                        "    }"));
    }

}
