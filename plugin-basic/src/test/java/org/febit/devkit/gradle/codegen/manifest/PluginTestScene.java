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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.io.FileUtils;
import org.gradle.api.JavaVersion;
import org.gradle.api.internal.TaskInternal;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.tasks.TaskContainerInternal;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.testfixtures.ProjectBuilder;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.function.Consumer;

import static org.febit.devkit.gradle.codegen.manifest.CodegenManifestRegister.TASK_GENERATE_MANIFEST;
import static org.junit.jupiter.api.Assertions.*;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class PluginTestScene {

    private final File projectDir;
    private final ProjectInternal project;

    static void executeTask(TaskInternal task) {
        task.getTaskActions()
                .forEach(action -> action.execute(task));
    }

    public boolean isFileExists(String path) {
        return new File(projectDir, path).exists();
    }

    public File file(String path) {
        return new File(projectDir, path);
    }

    public TaskContainerInternal tasks() {
        return project.getTasks();
    }

    public CodegenManifestExtension extension() {
        return project.getExtensions().getByType(CodegenManifestExtension.class);
    }

    public void execute() {
        project.evaluate();
        var codegenTask = assertDoesNotThrow(() ->
                (CodegenManifestTask) tasks().getByName(TASK_GENERATE_MANIFEST)
        );
        executeTask(codegenTask);
    }

    public static PluginTestScene create(
            @lombok.NonNull
            String template,
            @lombok.NonNull
            String name
    ) throws IOException {
        return create(
                template,
                name,
                null
        );
    }

    public Properties loadProperties(File file) {
        var props = new Properties();
        try (var reader = new InputStreamReader(
                FileUtils.openInputStream(file),
                StandardCharsets.UTF_8
        )) {
            props.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return props;
    }

    public static PluginTestScene create(
            @lombok.NonNull
            String template,
            @lombok.NonNull
            String name,
            @Nullable
            Consumer<File> projectDirCustomizer
    ) throws IOException {
        var projectDir = new File("./build/test-plugin-scenes/codegen-manifest/" + name);
        FileUtils.copyDirectory(new File("./src/test/scenes-codegen-manifest/" + template), projectDir);

        if (projectDirCustomizer != null) {
            projectDirCustomizer.accept(projectDir);
        }

        var project = (ProjectInternal) ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .withName("manifest-codegen-plugin-test-" + name)
                .build();

        initPlugin(project);
        return new PluginTestScene(projectDir, project);
    }

    private static void initPlugin(ProjectInternal project) {
        var repos = project.getRepositories();
        // TODO remote repos
        repos.mavenCentral();

        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(CodegenManifestPlugin.class);

        var javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        javaPluginExtension.setSourceCompatibility(JavaVersion.VERSION_11);
        javaPluginExtension.setTargetCompatibility(JavaVersion.VERSION_11);
    }
}
