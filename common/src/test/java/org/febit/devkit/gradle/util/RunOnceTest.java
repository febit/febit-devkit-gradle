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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunOnceTest {

    @Nested
    class RunIfNot {

        @Test
        void runsExactlyOnce() {
            var count = new AtomicInteger();
            var once = RunOnce.of(count::incrementAndGet);
            assertFalse(once.isRan());
            once.runIfNot();
            once.runIfNot();
            once.runIfNot();
            assertTrue(once.isRan());
            assertEquals(1, count.get());
        }

        @Test
        void concurrentCallsRunOnce() throws InterruptedException {
            var count = new AtomicInteger();
            var once = RunOnce.of(count::incrementAndGet);
            int threads = 32;
            List<Thread> ts = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                var t = new Thread(once::runIfNot);
                ts.add(t);
                t.start();
            }
            for (var t : ts) {
                t.join();
            }
            assertEquals(1, count.get());
        }
    }

    @Nested
    class IfRan {

        @Test
        void runsOnlyAfterTaskRan() {
            var count = new AtomicInteger();
            var once = RunOnce.of(() -> {
            });
            once.ifRan(count::incrementAndGet);
            assertEquals(0, count.get());
            once.runIfNot();
            once.ifRan(count::incrementAndGet);
            assertEquals(1, count.get());
        }
    }
}
