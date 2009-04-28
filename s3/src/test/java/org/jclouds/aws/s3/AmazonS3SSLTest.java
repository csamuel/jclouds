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
package org.jclouds.aws.s3;

import org.testng.annotations.Test;

import java.util.Properties;

/**
 * // TODO: Adrian: Document this!
 *
 * @author Adrian Cole
 */
@Test(groups = "unit", sequential = true, testName = "s3.AmazonS3SSLTest")
public class AmazonS3SSLTest extends AmazonS3Test {
    @Override
    protected Properties buildS3Properties(String AWSAccessKeyId, String AWSSecretAccessKey) {
        Properties properties = super.buildS3Properties(AWSAccessKeyId, AWSSecretAccessKey);
        properties.setProperty("jclouds.http.secure", Boolean.toString(true));
	properties.setProperty("jclouds.http.port", "443");
        return properties;
    }

}