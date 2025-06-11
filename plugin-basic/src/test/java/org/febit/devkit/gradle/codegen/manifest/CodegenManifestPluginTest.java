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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.febit.devkit.gradle.codegen.manifest.CodegenManifestExtension.CODEGEN_MANIFEST;
import static org.junit.jupiter.api.Assertions.*;

class CodegenManifestPluginTest {

    static final String TARGET_DIR = "build/generated/resources/" + CODEGEN_MANIFEST;
    static final String PROJ_GROUP = "org.febit.tests";
    static final String PROJ_VERSION = "1.1.0-beta.1";

    @Test
    void basic() throws IOException {
        var scene = PluginTestScene.create("basic", "basic");
        var project = scene.project();
        project.setGroup(PROJ_GROUP);
        project.setVersion(PROJ_VERSION);

        var extension = scene.extension();

        var directs = Map.of(
                "git.commitId", "01234567",
                "git.url", "https://abc/repo.git",
                "build.buildTime", "2023-10-01T12:00:00Z",
                "build.buildTimeEpochMilli", "1701158400000",
                "test.zh-cn", "测试"
        );
        var systemProps = List.of(
                "user.home",
                "java.home",
                "file.separator",
                "file.encoding",
                "not-exists"
        );
        var envs = List.of(
                "PATH",
                "HOME",
                "NOT_EXISTS"
        );

        var stub = new ManifestStub();
        stub.directProperty("stub.id", "1");
        stub.setPath("abc/test.properties");
        directs.forEach(stub::directProperty);
        systemProps.forEach(k ->
                stub.sysProperty("props." + k, k)
        );
        envs.forEach(k ->
                stub.sysEnvProperty("env." + k.toLowerCase(), k)
        );
        extension.getManifestStubs().add(stub);

        var stub2 = new ManifestStub();
        stub2.setPath("manifest-2.properties");
        stub2.directProperty("stub.id", "2");
        extension.getManifestStubs().add(stub2);
        scene.execute();

        var manifest1 = scene.file(TARGET_DIR + "/abc/test.properties");
        assertTrue(manifest1.exists());

        var props = scene.loadProperties(manifest1);
        assertEquals("1", props.getProperty("stub.id"));
        directs.forEach((key, value) -> {
            assertEquals(value, props.getProperty(key));
        });
        systemProps.forEach(k -> {
            var propKey = "props." + k;
            if (System.getProperty(k) != null) {
                assertEquals(System.getProperty(k), props.getProperty(propKey));
            } else {
                assertNull(props.getProperty(propKey));
            }
        });
        envs.forEach(k -> {
            var propKey = "env." + k.toLowerCase();
            if (System.getenv(k) != null) {
                assertEquals(System.getenv(k), props.getProperty(propKey));
            } else {
                assertNull(props.getProperty(propKey));
            }
        });

        var manifest2 = scene.file(TARGET_DIR + "/manifest-2.properties");
        assertTrue(manifest2.exists());
        var props2 = scene.loadProperties(manifest2);
        assertEquals("2", props2.getProperty("stub.id"));

    }

}
