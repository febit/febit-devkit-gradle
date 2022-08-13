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
import lombok.Setter;
import org.gradle.api.Project;

import javax.inject.Inject;
import java.io.File;

public class CodegenModuleExtension {

    private static final String CODEGEN_MODULE = "codegen-module";

    @Getter
    @Setter
    private File gitDir;

    @Getter
    @Setter
    private File generatedSourceDir;

    @Getter
    @Setter
    private File generatedResourceDir;

    @Getter
    @Setter
    private String moduleClassName;

    @Inject
    public CodegenModuleExtension(Project project) {
        this.gitDir = new File(project.getRootDir(), ".git");
        this.generatedSourceDir = new File(project.getBuildDir(), "generated/sources/" + CODEGEN_MODULE);
        this.generatedResourceDir = new File(project.getBuildDir(), "generated/resources/" + CODEGEN_MODULE);
    }
}

