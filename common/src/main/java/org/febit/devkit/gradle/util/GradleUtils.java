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

import groovy.lang.Closure;
import lombok.experimental.UtilityClass;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import javax.annotation.Nullable;
import java.text.MessageFormat;

@UtilityClass
public class GradleUtils {

    private static final String SOURCE_SET_MAIN = "main";

    public static void print(Exception ex) {
        ex.printStackTrace();
    }

    public static void print(String text) {
        System.out.print(text);
    }

    public static void println(String text) {
        System.out.println(text);
    }

    public static void println(String text, Object... args) {
        println(MessageFormat.format(text, args));
    }

    public static void afterPlugin(PluginContainer plugins, Class<? extends Plugin> pluginType, Runnable action) {

        // Run if plugin already applied.
        if (plugins.hasPlugin(pluginType)) {
            action.run();
            return;
        }

        // Add callback when plugin added
        plugins.whenPluginAdded(plugin -> {
            if (pluginType.isAssignableFrom(plugin.getClass())) {
                action.run();
            }
        });
    }

    public static SourceSetContainer sourceSets(Project project) {
        return project.getExtensions()
                .getByType(JavaPluginExtension.class)
                .getSourceSets();
    }

    public static SourceSet mainSourceSet(Project project) {
        return sourceSets(project).getByName(SOURCE_SET_MAIN);
    }

    public static <T> T to(@Nullable Closure<?> closure, T target) {
        if (closure == null) {
            return target;
        }
        closure.setDelegate(target);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
        return target;
    }

}
