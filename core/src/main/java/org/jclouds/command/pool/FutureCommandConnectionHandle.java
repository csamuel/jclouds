/**
 *
 * Copyright (C) 2009 Adrian Cole <adriancole@jclouds.org>
 *
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 */
package org.jclouds.command.pool;

import com.google.inject.assistedinject.Assisted;
import org.jclouds.Logger;
import org.jclouds.command.FutureCommand;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * // TODO: Adrian: Document this!
 *
 * @author Adrian Cole
 */
public abstract class FutureCommandConnectionHandle<C> {
    protected final BlockingQueue<C> available;
    protected final Semaphore maxConnections;
    protected final Semaphore completed;
    protected C conn;
    @SuppressWarnings("unchecked")
    protected FutureCommand operation;
    protected final Logger logger;

    @SuppressWarnings("unchecked")
    public FutureCommandConnectionHandle(java.util.logging.Logger logger, Semaphore maxConnections, @Assisted FutureCommand operation, @Assisted C conn, BlockingQueue<C> available) throws InterruptedException {
        this.maxConnections = maxConnections;
        this.operation = operation;
        this.conn = conn;
        this.available = available;
        this.logger = new Logger(logger);
        this.completed = new Semaphore(1);
        completed.acquire();
    }

    @SuppressWarnings("unchecked")
    public FutureCommand getOperation() {
        return operation;
    }

    public abstract void startConnection();

    public boolean isCompleted() {
        return (completed.availablePermits() == 1);
    }

    public void release() throws InterruptedException {
        if (isCompleted()) {
            return;
        }
        logger.trace("%1s - %2d - releasing to pool", conn, conn.hashCode());
        available.put(conn);
        conn = null;
        operation = null;
        completed.release();
    }

    public void cancel() throws IOException {
        if (isCompleted()) {
            return;
        }
        if (conn != null) {
            logger.trace("%1s - %2d - cancelled; shutting down connection", conn, conn.hashCode());
            try {
                shutdownConnection();
            } finally {
                conn = null;
                operation = null;
                maxConnections.release();
            }
        }
        completed.release();
    }

    public abstract void shutdownConnection() throws IOException;

    public void waitFor() throws InterruptedException {
        completed.acquire();
        completed.release();
    }
}