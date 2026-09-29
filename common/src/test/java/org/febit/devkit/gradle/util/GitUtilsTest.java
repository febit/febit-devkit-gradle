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
import org.gradle.api.file.Directory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GitUtilsTest {

    @Test
    void noHeadFile(@TempDir File gitDir) {
        assertEquals(GitUtils.CANNOT_RESOLVED, GitUtils.resolveHeadCommitId(gitDir));
    }

    @Test
    void detachedHead(@TempDir File gitDir) throws IOException {
        FileUtils.write(new File(gitDir, "HEAD"), "  abc123\n", StandardCharsets.UTF_8);
        assertEquals("abc123", GitUtils.resolveHeadCommitId(gitDir));
    }

    @Test
    void symbolicRef(@TempDir File gitDir) throws IOException {
        FileUtils.write(new File(gitDir, "HEAD"), "ref: refs/heads/master\n", StandardCharsets.UTF_8);
        FileUtils.write(new File(gitDir, "refs/heads/master"), "  deadbeef\n", StandardCharsets.UTF_8);
        assertEquals("deadbeef", GitUtils.resolveHeadCommitId(gitDir));
    }

    @Test
    void missingRefFile(@TempDir File gitDir) throws IOException {
        FileUtils.write(new File(gitDir, "HEAD"), "ref: refs/heads/nope\n", StandardCharsets.UTF_8);
        assertEquals(GitUtils.CANNOT_RESOLVED, GitUtils.resolveHeadCommitId(gitDir));
    }

    @Test
    void viaDirectoryArgument(@TempDir File gitDir) throws IOException {
        FileUtils.write(new File(gitDir, "HEAD"), "ref: refs/heads/master\n", StandardCharsets.UTF_8);
        FileUtils.write(new File(gitDir, "refs/heads/master"), "cafe\n", StandardCharsets.UTF_8);
        Directory dir = mock(Directory.class);
        when(dir.getAsFile()).thenReturn(gitDir);
        assertEquals("cafe", GitUtils.resolveHeadCommitId(dir));
    }
}
