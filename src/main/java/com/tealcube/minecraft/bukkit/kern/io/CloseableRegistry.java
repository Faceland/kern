/*
 * This file is part of kern, licensed under the MIT License (MIT)
 *
 * Copyright (c) 2016 Teal Cube Games <http://tealcubegames.com/>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.tealcube.minecraft.bukkit.kern.io;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * CloseableRegistry holds multiple {@link java.io.Closeable}s and enables closing all of them at once.
 */
public final class CloseableRegistry {

    private final Deque<Closeable> closeableDeque;

    /**
     * Instantiate a new CloseableRegistry.
     */
    public CloseableRegistry() {
        closeableDeque = new ArrayDeque<>(4);
    }

    /**
     * Register a {@link java.io.Closeable} or a subclass of {@link java.io.Closeable}.
     *
     * @param closeable Closeable to be registered
     * @param <C>       Closeable or a subclass of Closeable
     * @return passed-in Closeable
     */
    public <C extends Closeable> C register(C closeable) {
        closeableDeque.push(closeable);
        return closeable;
    }

    /**
     * Register a {@link java.sql.Connection} or a subclass of {@link java.sql.Connection}.
     *
     * @param connection Connection to be registered
     * @param <C>        Connection or a subclass of Connection
     * @return passed-in Connection
     */
    public <C extends Connection> C register(final C connection) {
        closeableDeque.push(new Closeable() {
            @Override
            public void close() throws IOException {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new IOException("failed to close", e);
                }
            }
        });
        return connection;
    }

    /**
     * Register a {@link java.sql.Statement} or a subclass of {@link java.sql.Statement}.
     *
     * @param statement Statement to be registered
     * @param <C>       Statement or a subclass of Statement
     * @return passed-in Statement
     */
    public <C extends Statement> C register(final C statement) {
        closeableDeque.push(new Closeable() {
            @Override
            public void close() throws IOException {
                try {
                    statement.close();
                } catch (SQLException e) {
                    throw new IOException("failed to close", e);
                }
            }
        });
        return statement;
    }

    /**
     * Register a {@link java.sql.ResultSet} or a subclass of {@link java.sql.ResultSet}.
     *
     * @param resultSet ResultSet to be registered
     * @param <C>       ResultSet or a subclass of ResultSet
     * @return passed-in ResultSet
     */
    public <C extends ResultSet> C register(final C resultSet) {
        closeableDeque.push(new Closeable() {
            @Override
            public void close() throws IOException {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    throw new IOException("failed to close", e);
                }
            }
        });
        return resultSet;
    }

    /**
     * Attemps to close all registered {@link java.io.Closeable}s. Ignores thrown Exceptions.
     */
    public void closeQuietly() {
        try {
            close();
        } catch (IOException ignored) {
            // do nothing
        }
    }

    /**
     * Attempts to close all registered {@link java.io.Closeable}s.
     *
     * @throws java.io.IOException thrown if unable to close a registered closeable
     */
    public void close() throws IOException {
        while (!closeableDeque.isEmpty()) {
            Closeable closeable = closeableDeque.pop();
            try {
                closeable.close();
            } catch (IOException e) {
                throw new IOException("unable to close", e);
            }
        }
    }

}
