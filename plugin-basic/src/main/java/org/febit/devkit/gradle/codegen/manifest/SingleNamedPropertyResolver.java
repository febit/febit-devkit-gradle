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

import lombok.RequiredArgsConstructor;
import org.febit.devkit.gradle.util.SerializableSupplier;

import java.io.Serializable;
import java.util.function.BiConsumer;

@RequiredArgsConstructor(
        staticName = "of"
)
public class SingleNamedPropertyResolver implements PropertyResolver {

    private final String name;
    private final SerializableSupplier<Serializable> supplier;

    @Override
    public void resolve(BiConsumer<String, Serializable> sink) {
        var value = this.supplier.get();
        if (value == null) {
            return;
        }
        sink.accept(name, value);
    }

    public static SingleNamedPropertyResolver ofDirect(String name, Serializable value) {
        return new SingleNamedPropertyResolver(name, () -> value);
    }

    public static SingleNamedPropertyResolver ofSysEnv(String name, String env) {
        return new SingleNamedPropertyResolver(name, () -> System.getenv(env));
    }

    public static SingleNamedPropertyResolver ofSysProperty(String name, String prop) {
        return new SingleNamedPropertyResolver(name, () -> System.getProperty(prop));
    }
}
