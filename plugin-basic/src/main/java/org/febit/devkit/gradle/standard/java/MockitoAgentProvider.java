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

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.process.CommandLineArgumentProvider;

import java.util.List;

public abstract class MockitoAgentProvider implements CommandLineArgumentProvider {

    private static final String JAVAAGENT_PREFIX = "-javaagent:";

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getAgentJar();

    @Override
    public Iterable<String> asArguments() {
        var jars = getAgentJar().getFiles();
        if (jars.isEmpty()) {
            return List.of();
        }
        // one -javaagent argument per jar; joining them with the path separator is not valid
        var arguments = jars.stream()
                .map(jar -> JAVAAGENT_PREFIX + jar.getAbsolutePath())
                .toList();
        Logging.getLogger(MockitoAgentProvider.class)
                .lifecycle("Mockito agent: {}", String.join(", ", arguments));
        return arguments;
    }
}
