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

import org.apache.commons.collections4.properties.SortedProperties;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class PropertiesManifestGenerator implements ManifestGenerator {

    @Override
    public void generate(ManifestStub manifestStub, OutputStream out) throws IOException {
        var props = new SortedProperties();
        manifestStub.getPropertyResolvers()
                .forEach(resolver -> resolver.resolve(props::put));

        var writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        props.store(writer, null);
    }
}
