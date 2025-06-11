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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.febit.devkit.gradle.task.CodegenTask;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

@Slf4j
@CacheableTask
public abstract class CodegenManifestTask extends DefaultTask implements CodegenTask {

    @Input
    protected abstract ListProperty<ManifestStub> getManifestStubs();

    @OutputDirectory
    protected abstract DirectoryProperty getGeneratedResourceDir();

    @TaskAction
    public void run() {
        getManifestStubs().get().forEach(this::emitManifest);
    }

    private void emitManifest(ManifestStub stub) {
        log.info("Emit manifest: {}", stub.getPath());
        var srcDir = getGeneratedResourceDir().get().getAsFile();
        var target = new File(srcDir, stub.getPath());
        var generator = stub.getFormat().generator();
        try (var out = FileUtils.openOutputStream(target)) {
            generator.generate(stub, out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
