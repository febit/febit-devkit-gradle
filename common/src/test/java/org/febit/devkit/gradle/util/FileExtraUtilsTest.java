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

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileExtraUtilsTest {

    @Nested
    class WriteJavaClass {

        @Test
        void toRootWhenPkgNull(@TempDir File root) throws IOException {
            FileExtraUtils.writeJavaClass(root, null, "Foo", "content");
            var f = new File(root, "Foo.java");
            assertTrue(f.isFile());
            assertEquals("content", read(f));
        }

        @Test
        void underPackageDirectory(@TempDir File root) throws IOException {
            FileExtraUtils.writeJavaClass(root, "a.b.c", "Foo", "content");
            var f = new File(root, "a/b/c/Foo.java");
            assertTrue(f.isFile());
            assertEquals("content", read(f));
        }
    }

    @Nested
    class WriteIfNotMatch {

        @Test
        void writesWhenAbsent(@TempDir File root) throws IOException {
            var f = new File(root, "x.txt");
            FileExtraUtils.writeIfNotMatch(f, "abc");
            assertEquals("abc", read(f));
        }

        @Test
        void skipsWhenUnchanged(@TempDir File root) throws IOException, InterruptedException {
            var f = new File(root, "x.txt");
            FileExtraUtils.write(f, "abc");
            var before = f.lastModified();
            Thread.sleep(20);
            FileExtraUtils.writeIfNotMatch(f, "abc");
            assertEquals(before, f.lastModified());
            assertEquals("abc", read(f));
        }

        @Test
        void overwritesWhenChanged(@TempDir File root) throws IOException {
            var f = new File(root, "x.txt");
            FileExtraUtils.write(f, "abc");
            FileExtraUtils.writeIfNotMatch(f, "xyz");
            assertEquals("xyz", read(f));
        }
    }

    @Test
    void writesUtf8Content(@TempDir File root) throws IOException {
        var f = new File(root, "x.txt");
        FileExtraUtils.write(f, "hello");
        assertEquals("hello", read(f));
    }

    @Test
    void trimsWhitespace(@TempDir File root) throws IOException {
        var f = new File(root, "x.txt");
        FileUtils.write(f, "  hello  \n", StandardCharsets.UTF_8);
        assertEquals("hello", FileExtraUtils.readAndTrim(f));
    }

    static String read(File f) throws IOException {
        return FileUtils.readFileToString(f, StandardCharsets.UTF_8);
    }
}
