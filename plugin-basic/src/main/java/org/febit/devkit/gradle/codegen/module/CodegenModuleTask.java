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
import org.febit.devkit.gradle.task.CodegenTask;
import org.febit.devkit.gradle.util.FileExtraUtils;
import org.febit.devkit.gradle.util.FolderUtils;
import org.febit.devkit.gradle.util.GitUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@CacheableTask
public abstract class CodegenModuleTask extends DefaultTask implements CodegenTask {

    @Input
    protected abstract Property<String> getVersion();

    @Input
    protected abstract Property<String> getArtifactId();

    @Input
    protected abstract Property<String> getGroupId();

    @OutputDirectory
    protected abstract DirectoryProperty getGeneratedSourceDir();

    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    protected abstract DirectoryProperty getGitDir();

    @Input
    protected abstract ListProperty<CodegenModuleExtension.ModuleEntry> getModules();

    @Inject
    public CodegenModuleTask() {
    }

    @TaskAction
    public void run() {
        getModules().get().forEach(this::emitModule);
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

    private void emitModule(CodegenModuleExtension.ModuleEntry entry) {
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

        var commitId = GitUtils.resolveHeadCommitId(getGitDir().get());
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
                "groupId", getGroupId().get(),
                "artifactId", getArtifactId().get(),
                "version", getVersion().get()
        );

        var buf = new StringWriter();

        try {
            loadTemplate(entry)
                    .make(new HashMap<>(params))
                    .writeTo(buf);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        var srcDir = getGeneratedSourceDir().get().getAsFile();
        FolderUtils.mkdirs(srcDir);
        FileExtraUtils.writeJavaClass(srcDir, pkg, classSimpleName, buf.toString());
    }

}
