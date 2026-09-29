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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.tabletest.junit.TableTest;
import org.tabletest.junit.TypeConverter;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JavaUtilsTest {

    private static final Map<String, Class<?>> PRIMITIVES = Map.of(
            "int", int.class,
            "long", long.class,
            "double", double.class,
            "float", float.class,
            "boolean", boolean.class,
            "byte", byte.class,
            "char", char.class,
            "short", short.class,
            "void", void.class
    );

    @TypeConverter
    static Class<?> classFromName(String name) {
        Class<?> primitive = PRIMITIVES.get(name);
        if (primitive != null) {
            return primitive;
        }
        int depth = 0;
        String base = name;
        while (base.endsWith("[]")) {
            depth++;
            base = base.substring(0, base.length() - 2);
        }
        Class<?> baseClass = PRIMITIVES.getOrDefault(base, forName(base));
        if (depth == 0) {
            return baseClass;
        }
        return Array.newInstance(baseClass, new int[depth]).getClass();
    }

    private static Class<?> forName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot resolve class: " + name, e);
        }
    }

    @TableTest("""
            Scenario         | word         | expectKeyword
            reserved word    | class        | true
            primitive type   | int          | true
            null literal     | null         | true
            access modifier  | public       | true
            void type        | void         | true
            synchronization  | synchronized | true
            assertion        | assert       | true
            plain identifier | foo          | false
            class name       | Bar          | false
            type name        | String       | false
            underscore name  | _x           | false
            mixed identifier | myVar1       | false
            """)
    void isKeyword(String word, boolean expectKeyword) {
        assertEquals(expectKeyword, JavaUtils.isKeyword(word));
    }

    @TableTest("""
            Scenario       | fullName | expectPackage
            qualified name | a.b.C    | a.b
            simple name    | C        | C
            trailing dot   | a.       | a
            """)
    void pkg(String fullName, String expectPackage) {
        assertEquals(expectPackage, JavaUtils.pkg(fullName));
    }

    @TableTest("""
            Scenario       | fullName | expectSimpleName
            qualified name | a.b.C    | C
            simple name    | C        | C
            """)
    void classSimpleName(String fullName, String expectSimpleName) {
        assertEquals(expectSimpleName, JavaUtils.classSimpleName(fullName));
    }

    @TableTest("""
            Scenario      | input | expectUpperFirst
            empty string  | ''    | ''
            single letter | a     | A
            already upper | Ab    | Ab
            lower word    | abc   | Abc
            """)
    void upperFirst(String input, String expectUpperFirst) {
        assertEquals(expectUpperFirst, JavaUtils.upperFirst(input));
    }

    @TableTest("""
            Scenario             | cls             | pkg     | expectInPackage
            exact package        | com.foo.Bar     | com.foo | true
            sub package          | com.foo.bar.Baz | com.foo | false
            prefix without dot   | com.fooBar      | com.foo | false
            same as package      | com.foo         | com.foo | false
            shorter than package | com.fo          | com.foo | false
            other package        | foo.bar.Baz     | foo     | false
            """)
    void isInPackage(String cls, String pkg, boolean expectInPackage) {
        assertEquals(expectInPackage, JavaUtils.isInPackage(cls, pkg));
    }

    @TableTest("""
            Scenario      | input              | expectFinalType
            plain type    | java.lang.String   | java.lang.String
            primitive dir | int[]              | int
            object array  | java.lang.String[] | java.lang.String
            three levels  | int[][][]          | int
            """)
    void resolveFinalComponentType(Class<?> input, Class<?> expectFinalType) {
        assertSame(expectFinalType, JavaUtils.resolveFinalComponentType(input));
    }

    @Nested
    @SuppressWarnings({
            "unused",
            "DeprecatedIsStillUsed",
            "InnerClassMayBeStatic",
            "ConstantValue"
    })
    class IsDeprecated {

        @Test
        void elementIsNull() {
            assertFalse(JavaUtils.isDeprecated((AnnotatedElement) null));
        }

        @Test
        void elementIsDeprecated() {
            assertTrue(JavaUtils.isDeprecated(DeprecatedType.class));
        }

        @Test
        void elementIsNotDeprecated() {
            assertFalse(JavaUtils.isDeprecated(NonDeprecatedType.class));
        }

        @Test
        void propertyIsNull() {
            assertFalse(JavaUtils.isDeprecated((PropertyDescriptor) null));
        }

        @Test
        void readAccessorIsDeprecated() throws IntrospectionException {
            assertTrue(JavaUtils.isDeprecated(new PropertyDescriptor("x", Bean.class)));
        }

        @Test
        void noAccessorIsDeprecated() throws IntrospectionException {
            assertFalse(JavaUtils.isDeprecated(new PropertyDescriptor("y", Bean.class)));
        }

        @Deprecated
        class DeprecatedType {
        }

        class NonDeprecatedType {
        }

        class Bean {

            @Deprecated
            public String getX() {
                return null;
            }

            public void setX(String x) {
            }

            public String getY() {
                return null;
            }

            public void setY(String y) {
            }
        }
    }
}
