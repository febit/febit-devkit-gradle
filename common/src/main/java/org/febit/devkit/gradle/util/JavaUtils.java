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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JavaUtils {

    private static final Set<String> KEYWORDS = Set.of(
            "private",
            "protected",
            "public",

            "import",
            "package",
            "abstract",
            "extends",
            "implements",
            "throws",
            "class",
            "interface",

            "const",
            "native",
            "final",
            "static",
            "strictfp",
            "synchronized",
            "transient",
            "volatile",

            "null",
            "void",
            "boolean",
            "byte",
            "char",
            "double",
            "float",
            "int",
            "long",
            "short",

            "break",
            "case",
            "continue",
            "default",
            "do",
            "else",
            "for",
            "if",
            "return",
            "switch",
            "while",
            "try",
            "catch",
            "finally",
            "goto",

            "instanceof",
            "throw",
            "super",
            "this",
            "new",
            "assert"
    );

    public static boolean isKeyword(String word) {
        return KEYWORDS.contains(word);
    }

}
