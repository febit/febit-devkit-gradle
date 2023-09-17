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

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import org.apache.commons.lang3.StringUtils;
import org.febit.devkit.gradle.util.FileExtraUtils;
import org.febit.devkit.gradle.util.GitUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class CodegenModuleTask extends DefaultTask {

    @Inject
    public CodegenModuleTask() {
        setGroup(Constants.GROUP);
        setDescription("Generate module files.");
    }

    @TaskAction
    public void run() {
        var extension = getProject().getExtensions()
                .getByType(CodegenModuleExtension.class);

        emitModuleClass(extension);
    }

    private void emitModuleClass(CodegenModuleExtension extension) {
        extension.getModules().forEach(entry ->
                emitModuleClass(extension, entry)
        );
    }

    private Template loadTemplate(CodegenModuleExtension.ModuleEntry entry) {
        var tmplEngine = new SimpleTemplateEngine();
        try {
            var text = entry.getTemplate().resolve();
            return tmplEngine.createTemplate(text);
        } catch (ClassNotFoundException | IOException e) {
            throw new GradleException(
                    "Cannot resolve template for module'" + entry.getName() + "'.", e);
        }
    }

    private void emitModuleClass(CodegenModuleExtension extension, CodegenModuleExtension.ModuleEntry entry) {
        var classFullName = entry.getName();
        if (StringUtils.isEmpty(classFullName)) {
            return;
        }
        if (!classFullName.contains(".")) {
            throw new GradleException("Module name should be a FULL class name,"
                    + " default package is not supported.");
        }

        var pkg = StringUtils.substringBeforeLast(classFullName, ".");
        var classSimpleName = classFullName.contains(".")
                ? StringUtils.substringAfterLast(classFullName, ".")
                : classFullName;

        var proj = getProject();
        var commitId = GitUtils.resolveHeadCommitId(extension.getGitDir());
        var buildTime = Instant.ofEpochSecond(
                System.currentTimeMillis() / 1000
        );

        var params = Map.of(
                "classPackage", pkg,
                "classFullName", classFullName,
                "classSimpleName", classSimpleName,
                "buildTime", buildTime,
                "buildJdk", System.getProperty("java.version"),
                "commitId", commitId,
                "groupId", proj.getGroup(),
                "artifactId", proj.getName(),
                "version", proj.getVersion()
        );

        var buf = new StringWriter();

        try {
            loadTemplate(entry)
                    .make(new HashMap<>(params))
                    .writeTo(buf);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        var srcDir = extension.getGeneratedSourceDir();
        FileExtraUtils.writeJavaClass(srcDir, pkg, classSimpleName, buf.toString());
    }

}
