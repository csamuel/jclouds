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
package org.jclouds.aws.s3.commands.callables;

import java.io.IOException;
import java.io.InputStream;

import org.jclouds.Logger;
import org.jclouds.Utils;
import org.jclouds.http.HttpException;
import org.jclouds.http.HttpFutureCommand;

import com.google.inject.Inject;

/**
 * // TODO: Adrian: Document this!
 * 
 * @author Adrian Cole
 */
public class CopyObjectCallable extends
	HttpFutureCommand.ResponseCallable<Boolean> {

    @Inject
    public CopyObjectCallable(java.util.logging.Logger logger) {
	super(new Logger(logger));
    }

    public Boolean call() throws HttpException {
	if (getResponse().getStatusCode() == 404
		|| getResponse().getStatusCode() == 400) {
	    String reason = null;
	    try {
		reason = Utils.toStringAndClose(getResponse().getContent());
	    } catch (IOException e) {
		logger.error(e, "error consuming content");
	    }
	    throw new HttpException("Error copying source " + reason);
	} else if (getResponse().getStatusCode() == 200) {
	    String response = null;
	    InputStream content = getResponse().getContent();
	    if (content != null) {
		try {
		    response = Utils.toStringAndClose(content);
		    System.err.println("Copy response: " + response);
		} catch (IOException e) {
		    logger.error(e, "error consuming content");
		}
	    }
	    return true;
	} else {
	    throw new HttpException("Error copying object " + getResponse());
	}
    }

}