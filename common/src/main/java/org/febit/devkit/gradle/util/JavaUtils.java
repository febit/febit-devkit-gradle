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
import org.gradle.internal.impldep.org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedElement;
import java.util.Set;

@UtilityClass
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

    public static String pkg(String fullName) {
        return StringUtils.substringBeforeLast(fullName, ".");
    }

    public static String classSimpleName(String fullName) {
        return StringUtils.substringAfterLast(fullName, ".");
    }

    public static boolean isDeprecated(@Nullable AnnotatedElement element) {
        return element != null && element.isAnnotationPresent(Deprecated.class);
    }

    public static boolean isDeprecated(@Nullable PropertyDescriptor prop) {
        if (prop == null) {
            return false;
        }
        return isDeprecated(prop.getReadMethod())
                || isDeprecated(prop.getWriteMethod());
    }

    public static boolean isInPackage(String cls, String pkg) {
        int pkgLen = pkg.length();
        if (cls.length() <= pkgLen) {
            return false;
        }
        if (cls.charAt(pkgLen) != '.') {
            return false;
        }
        if (!cls.startsWith(pkg)) {
            return false;
        }
        return cls.indexOf('.', pkgLen + 1) < 0;
    }

    public static Class<?> resolveFinalComponentType(Class<?> cls) {
        if (!cls.isArray()) {
            return cls;
        }
        return resolveFinalComponentType(cls.getComponentType());
    }

    public static String upperFirst(String ident) {
        return ident.substring(0, 1).toUpperCase()
                + ident.substring(1);
    }
}
