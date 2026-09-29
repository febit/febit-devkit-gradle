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

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.testing.Test;

import org.febit.devkit.gradle.plugin.Setup;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.devkit.gradle.util.RunOnce;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class TestTaskSetup implements Setup {

    private static final String TEST_CLASSES_PATTERN = "**/*Test.class";
    private static final String TAG_INTEGRATION = "integration";

    private static final String INTEGRATION_TEST_TASK = "integrationTest";
    private static final String INTEGRATION_TEST_DESCRIPTION =
            "Runs integration tests (JUnit tag \"integration\").";

    private static final String TMP_TEST_CASES = "tmp/tests";
    private static final String PROP_JAVA_IO_TMPDIR = "java.io.tmpdir";

    private final Project project;

    private final RunOnce applyOnce = RunOnce.of(this::apply);
    private final RunOnce configTestTaskOnce = RunOnce.of(this::configTestTask);

    @Override
    public void setup() {
        GradleUtils.afterPlugin(project.getPlugins(), JavaBasePlugin.class, applyOnce::runIfNot);
        // The "test" task is created by the java plugin (not by java-base), so hook on it as well:
        // this keeps the plugin working no matter whether "java" is applied before or after.
        GradleUtils.afterPlugin(project.getPlugins(), JavaPlugin.class, configTestTaskOnce::runIfNot);
    }

    private void apply() {
        var tmpDir = project.getLayout()
                .getBuildDirectory()
                .dir(TMP_TEST_CASES)
                .get()
                .getAsFile();
        project.getTasks().withType(Test.class).configureEach(test -> {
            test.systemProperty(PROP_JAVA_IO_TMPDIR, tmpDir.getAbsolutePath());
            test.doFirst(task -> tmpDir.mkdirs());
        });

        project.getTasks().register(INTEGRATION_TEST_TASK, Test.class, task -> {
            var sourceSet = GradleUtils.sourceSets(project)
                    .getByName(SourceSet.TEST_SOURCE_SET_NAME);

            task.setDescription(INTEGRATION_TEST_DESCRIPTION);
            task.setTestClassesDirs(sourceSet.getOutput().getClassesDirs());
            task.setClasspath(sourceSet.getRuntimeClasspath());
            task.useJUnitPlatform(spec -> spec.includeTags(TAG_INTEGRATION));
            task.shouldRunAfter(JavaPlugin.TEST_TASK_NAME);
        });
    }

    private void configTestTask() {
        project.getTasks().named(JavaPlugin.TEST_TASK_NAME, Test.class, test -> {
            test.include(TEST_CLASSES_PATTERN);
            test.useJUnitPlatform(spec -> spec.excludeTags(TAG_INTEGRATION));
        });
    }
}
