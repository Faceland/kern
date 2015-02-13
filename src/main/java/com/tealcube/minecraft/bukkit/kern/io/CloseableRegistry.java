/*
 * This file is part of kern, licensed under the ISC License.
 *
 * Copyright (c) 2014-2015 Richard Harrah
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted,
 * provided that the above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
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
     * Attempts to close all registered {@link java.io.Closeable}s.
     *
     * @throws IOException thrown if unable to close a registered closeable
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

}
