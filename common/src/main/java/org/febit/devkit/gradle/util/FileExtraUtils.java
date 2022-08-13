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

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class FileExtraUtils {

    public static void writeJavaClass(
            File rootDir, @Nullable String pkg,
            String classSimpleName, CharSequence content
    ) {

        var pkgDir = pkg == null ? rootDir
                : new File(rootDir, pkg.replace('.', '/'));
        var file = new File(pkgDir, classSimpleName + ".java");

        FolderUtils.mkdirs(pkgDir);
        write(file, content);
    }

    public static void write(File file, CharSequence content) {
        try {
            FileUtils.write(file, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void writeIfNotMatch(File file, String content) {
        boolean match;
        try {
            match = file.exists()
                    && content.equals(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
        } catch (IOException ignoring) {
            match = false;
        }
        if (match) {
            return;
        }
        try {
            FileUtils.write(file, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String readAndTrim(File file) throws IOException {
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8)
                .trim();
    }
}
