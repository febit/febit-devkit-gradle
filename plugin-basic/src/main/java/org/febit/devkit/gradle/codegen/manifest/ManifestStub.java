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

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ManifestStub implements Serializable {
    private ManifestFileFormat format = ManifestFileFormat.PROPERTIES;
    private List<PropertyResolver> propertyResolvers = new ArrayList<>();

    private String path;

    public void property(PropertyResolver resolver) {
        propertyResolvers.add(resolver);
    }

    public void directProperty(String name, Serializable value) {
        property(SingleNamedPropertyResolver.ofDirect(name, value));
    }

    public void sysEnvProperty(String name, String env) {
        property(SingleNamedPropertyResolver.ofSysEnv(name, env));
    }

    public void sysProperty(String name, String prop) {
        property(SingleNamedPropertyResolver.ofSysProperty(name, prop));
    }
}
