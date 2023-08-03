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
package org.febit.devkit.gradle.standard.util;

import lombok.experimental.UtilityClass;
import org.gradle.api.Project;

@UtilityClass
public class StandardUtils {

    private static final String SUFFIX_BOM = "-bom";
    private static final String SUFFIX_DEPENDENCIES = "-dependencies";
    private static final String SUFFIX_SNAPSHOT = "-SNAPSHOT";

    public static boolean isSnapshot(Project project) {
        return String.valueOf(
                        project.getVersion()
                )
                .endsWith(SUFFIX_SNAPSHOT);
    }

    public static boolean isBom(Project project) {
        return project.getName()
                .endsWith(SUFFIX_BOM);
    }

    public static boolean isDependencies(Project project) {
        return project.getName()
                .endsWith(SUFFIX_DEPENDENCIES);
    }

}
