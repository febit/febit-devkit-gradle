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
package org.febit.devkit.gradle.codegen.manifest;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import lombok.Getter;
import org.febit.devkit.gradle.util.GradleUtils;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;

import javax.inject.Inject;

public class CodegenManifestExtension {

    static final String CODEGEN_MANIFEST = "codegen-manifest";

    @Getter
    private final DirectoryProperty generatedResourceDir;

    @Getter
    private final ListProperty<ManifestStub> manifestStubs;

    @Inject
    public CodegenManifestExtension(Project project) {
        var objects = project.getObjects();
        var buildDir = project.getLayout().getBuildDirectory();
        this.generatedResourceDir = objects.directoryProperty()
                .convention(buildDir.dir("generated/resources/" + CODEGEN_MANIFEST));
        this.manifestStubs = objects.listProperty(ManifestStub.class).empty();
    }

    public void manifest(@DelegatesTo(ManifestStub.class) Closure<?> closure) {
        var stub = new ManifestStub();
        GradleUtils.to(closure, stub);
        manifestStubs.add(stub);
    }

}

