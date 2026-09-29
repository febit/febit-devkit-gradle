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
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLClassLoader;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GradleUtilsTest {

    static class SomePlugin implements Plugin<Project> {
        @Override
        public void apply(Project project) {
            // no-op
        }
    }

    @Nested
    class AfterPlugin {

        @Test
        @SuppressWarnings("unchecked")
        void runsWhenAlreadyApplied() {
            var plugins = mock(PluginContainer.class);
            when(plugins.hasPlugin(SomePlugin.class)).thenReturn(true);
            var ran = new boolean[]{false};
            GradleUtils.afterPlugin(plugins, SomePlugin.class, () -> ran[0] = true);
            assertTrue(ran[0]);
            verify(plugins, never()).whenPluginAdded(any(Action.class));
        }

        @Test
        void runsWhenMatchingAdded() {
            var plugins = mock(PluginContainer.class);
            when(plugins.hasPlugin(SomePlugin.class)).thenReturn(false);
            var ran = new boolean[]{false};
            GradleUtils.afterPlugin(plugins, SomePlugin.class, () -> ran[0] = true);
            assertFalse(ran[0]);
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Action<Plugin>> captor = ArgumentCaptor.forClass(Action.class);
            verify(plugins).whenPluginAdded(captor.capture());
            captor.getValue().execute(new SomePlugin());
            assertTrue(ran[0]);
        }

        @Test
        void ignoresNonMatching() {
            var plugins = mock(PluginContainer.class);
            when(plugins.hasPlugin(SomePlugin.class)).thenReturn(false);
            var ran = new boolean[]{false};
            GradleUtils.afterPlugin(plugins, SomePlugin.class, () -> ran[0] = true);
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Action<Plugin>> captor = ArgumentCaptor.forClass(Action.class);
            verify(plugins).whenPluginAdded(captor.capture());
            captor.getValue().execute(mock(Plugin.class));
            assertFalse(ran[0]);
        }
    }

    @Nested
    class To {

        @Test
        void nullClosureReturnsTarget() {
            var target = new StringBuilder();
            assertSame(target, GradleUtils.to(null, target));
        }

        @Test
        void runsWithDelegateFirst() {
            var target = new StringBuilder();
            var closure = new Closure<Void>(null) {
                @Override
                public Void call() {
                    ((StringBuilder) getDelegate()).append("hi");
                    return null;
                }
            };
            var result = GradleUtils.to(closure, target);
            assertSame(target, result);
            assertEquals("hi", target.toString());
            assertEquals(Closure.DELEGATE_FIRST, closure.getResolveStrategy());
            assertSame(target, closure.getDelegate());
        }
    }

    @Nested
    class Println {

        @Test
        void writesToStdout() {
            var buf = new ByteArrayOutputStream();
            var old = System.out;
            System.setOut(new PrintStream(buf));
            try {
                GradleUtils.println("hello");
            } finally {
                System.setOut(old);
            }
            assertTrue(buf.toString().contains("hello"));
        }

        @Test
        void formatsArguments() {
            var buf = new ByteArrayOutputStream();
            var old = System.out;
            System.setOut(new PrintStream(buf));
            try {
                GradleUtils.println("hi {0} {1}", "a", "b");
            } finally {
                System.setOut(old);
            }
            assertTrue(buf.toString().contains("hi a b"));
        }
    }

    @Test
    void resolvesBuildDir() {
        var project = mock(Project.class);
        var layout = mock(ProjectLayout.class);
        var bp = mock(DirectoryProperty.class);
        @SuppressWarnings("unchecked")
        Property<File> fp = mock(Property.class);
        var build = new File("/build");
        when(project.getLayout()).thenReturn(layout);
        when(layout.getBuildDirectory()).thenReturn(bp);
        when(bp.getAsFile()).thenReturn(fp);
        when(fp.get()).thenReturn(build);
        assertSame(build, GradleUtils.buildDir(project));
    }

    @Test
    void resolvesSourceSets() {
        var project = mock(Project.class);
        var ext = mock(ExtensionContainer.class);
        var javaExt = mock(JavaPluginExtension.class);
        var ssc = mock(SourceSetContainer.class);
        when(project.getExtensions()).thenReturn(ext);
        when(ext.getByType(JavaPluginExtension.class)).thenReturn(javaExt);
        when(javaExt.getSourceSets()).thenReturn(ssc);
        assertSame(ssc, GradleUtils.sourceSets(project));
    }

    @Test
    void resolvesMainSourceSet() {
        var project = mock(Project.class);
        var ext = mock(ExtensionContainer.class);
        var javaExt = mock(JavaPluginExtension.class);
        var ssc = mock(SourceSetContainer.class);
        var main = mock(SourceSet.class);
        when(project.getExtensions()).thenReturn(ext);
        when(ext.getByType(JavaPluginExtension.class)).thenReturn(javaExt);
        when(javaExt.getSourceSets()).thenReturn(ssc);
        when(ssc.getByName("main")).thenReturn(main);
        assertSame(main, GradleUtils.mainSourceSet(project));
    }

    @Test
    void resolvesFilesToUrls() throws Exception {
        var conf = mock(Configuration.class);
        var f1 = new File("/a/x.jar");
        when(conf.resolve()).thenReturn(Set.of(f1));
        var urls = GradleUtils.toUrls(conf);
        assertEquals(1, urls.length);
        assertEquals(f1.toURI().toURL(), urls[0]);
    }

    @Test
    void createsClassLoader() throws IOException {
        var conf = mock(Configuration.class);
        var f1 = new File("/a/x.jar");
        when(conf.resolve()).thenReturn(Set.of(f1));
        try (URLClassLoader cl = GradleUtils.toClassLoader(conf)) {
            assertEquals(1, cl.getURLs().length);
        }
    }
}
