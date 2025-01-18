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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class CodegenModuleExtension {

    private static final String CODEGEN_MODULE = "codegen-module";

    private static final String BUILD_IN_TMPL_PREFIX = "/org/febit/devkit/gradle/codegen/module/";
    private static final String DEFAULT_TMPL = BUILD_IN_TMPL_PREFIX + "default.tmpl";
    private static final String FEBIT_TMPL = BUILD_IN_TMPL_PREFIX + "febit.tmpl";

    private static final TemplateResolver DEFAULT_TMPL_RESOLVER = fromClasspath(DEFAULT_TMPL);

    @Getter
    private final DirectoryProperty gitDir;

    @Getter
    private final DirectoryProperty generatedSourceDir;

    @Getter
    private final DirectoryProperty generatedResourceDir;

    @Getter
    @Setter
    private TemplateResolver defaultTemplate = DEFAULT_TMPL_RESOLVER;

    @Getter
    private final ListProperty<ModuleEntry> modules;

    @Inject
    public CodegenModuleExtension(Project project) {
        var objects = project.getObjects();

        var rootDir = project.getRootProject().getLayout().getProjectDirectory();
        this.gitDir = objects.directoryProperty()
                .convention(rootDir.dir(".git"));

        var buildDir = project.getLayout().getBuildDirectory();
        this.generatedSourceDir = objects.directoryProperty()
                .convention(buildDir.dir("generated/sources/" + CODEGEN_MODULE));
        this.generatedResourceDir = objects.directoryProperty()
                .convention(buildDir.dir("generated/resources/" + CODEGEN_MODULE));
        this.modules = objects.listProperty(ModuleEntry.class).empty();
    }

    public void module(String name) {
        module(name, defaultTemplate);
    }

    public void module(String name, TemplateResolver tmpl) {
        modules.add(
                ModuleEntry.of(name, tmpl)
        );
    }

    public static TemplateResolver febitTmpl() {
        return fromClasspath(FEBIT_TMPL);
    }

    public static TemplateResolver fromClasspath(String name) {
        return caching(() -> IOUtils.resourceToString(name, StandardCharsets.UTF_8));
    }

    public static TemplateResolver fromFile(String path) {
        return fromFile(new File(path));
    }

    public static TemplateResolver fromFile(File path) {
        return caching(() -> FileUtils.readFileToString(path, StandardCharsets.UTF_8));
    }

    @Getter
    @Setter
    @RequiredArgsConstructor(staticName = "of")
    public static class ModuleEntry implements Serializable {
        private final String name;
        private final TemplateResolver template;
    }

    @FunctionalInterface
    public interface TemplateResolver extends Serializable {

        String resolve() throws IOException;
    }

    private static TemplateResolver caching(TemplateResolver resolver) {
        return CachingTemplateResolver.caching(resolver);
    }

    @RequiredArgsConstructor(staticName = "caching")
    public static class CachingTemplateResolver implements TemplateResolver {

        private final TemplateResolver delegated;

        private transient String resolved;

        @Override
        public String resolve() throws IOException {
            String resolved = this.resolved;
            if (resolved != null) {
                return resolved;
            }
            resolved = delegated.resolve();
            this.resolved = resolved;
            return resolved;
        }
    }
}

