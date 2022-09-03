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

import org.apache.commons.lang3.StringUtils;
import org.febit.devkit.gradle.util.FileExtraUtils;
import org.febit.devkit.gradle.util.GitUtils;
import org.febit.devkit.gradle.util.GradleUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.time.Instant;

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

        var classFullName = extension.getModuleClassName();
        if (StringUtils.isEmpty(classFullName)) {
            GradleUtils.println("WARN: Skip generation of module class, "
                    + "since 'moduleClassName' option is absent in [" + Constants.EXTENSION + "]");
        }

        var pkg = classFullName.contains(".")
                ? StringUtils.substringBeforeLast(classFullName, ".")
                : null;
        var classSimpleName = classFullName.contains(".")
                ? StringUtils.substringAfterLast(classFullName, ".")
                : classFullName;

        var proj = getProject();
        var commitId = GitUtils.resolveHeadCommitId(extension.getGitDir());
        var builtAtSec = System.currentTimeMillis() / 1000;

        var buf = new StringBuilder();
        if (pkg != null) {
            buf.append("package ")
                    .append(pkg)
                    .append(";\n\n");
        }

        buf.append("import java.time.Instant;\n");
        buf.append("\n");

        buf.append("@SuppressWarnings({\n"
                + "        \"squid:S3400\" // Methods should not return constants\n"
                + "})\n");
        buf.append("public class ").append(classSimpleName).append(" {").append("\n")
                .append("\n");

        buf.append("    public static String groupId() {\n")
                .append("        return \"").append(proj.getGroup()).append("\";\n")
                .append("    }\n\n")
                .append("    public static String artifactId() {\n")
                .append("        return \"").append(proj.getName()).append("\";\n")
                .append("    }\n\n")
                .append("    public static String version() {\n")
                .append("        return \"").append(proj.getVersion()).append("\";\n")
                .append("    }\n\n")
                .append("    public static String commitId() {\n")
                .append("        return \"").append(commitId).append("\";\n")
                .append("    }\n\n")
                .append("    public static Instant builtAt() {\n")
                .append("        // At: ").append(Instant.ofEpochSecond(builtAtSec)).append("\n")
                .append("        return Instant.ofEpochSecond(").append(builtAtSec).append("L);\n")
                .append("    }\n\n");
        buf.append("}\n");

        var srcDir = extension.getGeneratedSourceDir();
        FileExtraUtils.writeJavaClass(srcDir, pkg, classSimpleName, buf);
    }

}
