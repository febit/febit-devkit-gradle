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
package org.febit.devkit.gradle.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FolderUtilsTest {

    @Test
    void createsDirectory(@TempDir File root) {
        var dir = new File(root, "a/b/c");
        FolderUtils.mkdirs(dir);
        assertTrue(dir.isDirectory());
    }

    @Test
    void existingDirectory(@TempDir File root) {
        var dir = new File(root, "x");
        assertTrue(dir.mkdirs());
        assertDoesNotThrow(() -> FolderUtils.mkdirs(dir));
        assertTrue(dir.isDirectory());
    }

    @Test
    void throwsWhenPathIsFile(@TempDir File root) throws IOException {
        var file = new File(root, "file");
        assertTrue(file.createNewFile());
        assertThrows(UncheckedIOException.class, () -> FolderUtils.mkdirs(file));
    }

    @Test
    void throwsWhenParentIsFile(@TempDir File root) throws IOException {
        var file = new File(root, "file");
        assertTrue(file.createNewFile());
        var child = new File(file, "sub");
        assertThrows(UncheckedIOException.class, () -> FolderUtils.mkdirs(child));
    }
}
