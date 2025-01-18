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
import org.gradle.api.file.Directory;

import java.io.File;

import static org.febit.devkit.gradle.util.FileExtraUtils.readAndTrim;

@UtilityClass
public class GitUtils {

    public static final String CANNOT_RESOLVED = "UNKNOWN";

    public static String resolveHeadCommitId(Directory gitDir) {
        return resolveHeadCommitId(gitDir.getAsFile());
    }

    public static String resolveHeadCommitId(File gitDir) {
        var headFile = new File(gitDir, "HEAD");
        if (!headFile.exists()) {
            return CANNOT_RESOLVED;
        }
        try {
            var headText = readAndTrim(headFile);
            if (!headText.startsWith("ref: ")) {
                return headText;
            }
            var refFile = new File(gitDir, headText.substring(5).trim());
            return readAndTrim(refFile);
        } catch (Exception e) {
            GradleUtils.println("WARN: Failed to read commit ID, use '"
                            + CANNOT_RESOLVED + "' instead: {0}",
                    e.getMessage());
            return CANNOT_RESOLVED;
        }
    }

}
