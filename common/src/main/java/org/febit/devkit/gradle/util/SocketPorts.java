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

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

@UtilityClass
public class SocketPorts {

    private static final int PING_TIMEOUT = 300;

    public static int detect() {
        try (var socket = new ServerSocket(0)) {
            while (!socket.isBound()) {
                Thread.sleep(50);
            }
            return socket.getLocalPort();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UncheckedIOException(new IOException("Cannot detect port, interrupted", e));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void ping(int port) throws IOException {
        var local = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
        try (var sock = new Socket()) {
            sock.setSoTimeout(PING_TIMEOUT);
            sock.connect(local, 500);
        }
    }

}
