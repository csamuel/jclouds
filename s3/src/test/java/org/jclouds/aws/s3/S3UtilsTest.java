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

import org.bouncycastle.util.encoders.Base64;
import org.jclouds.aws.PerformanceTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;

/**
 * // TODO: Adrian: Document this!
 *
 * @author Adrian Cole
 */
@Test(groups = "unit", sequential = true, testName = "s3.PerformanceTest")
public class S3UtilsTest extends PerformanceTest {

    @Test(dataProvider = "hmacsha1")
    void testBouncyCastleDigestSerialResponseTime(byte[] key, String message, String base64Digest) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException {
        for (int i = 0; i < 10000; i++)
            testBouncyCastleDigest(key, message, base64Digest);
    }

    @Test(dataProvider = "hmacsha1")
    void testBouncyCastleDigestParallelResponseTime(final byte[] key, final String message, final String base64Digest) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, InterruptedException, ExecutionException {
        CompletionService<Boolean> completer = new ExecutorCompletionService<Boolean>(exec);
        for (int i = 0; i < 10000; i++)
            completer.submit(new Callable<Boolean>() {
                public Boolean call() {
                    try {
                        testBouncyCastleDigest(key, message, base64Digest);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }
            });
        for (int i = 0; i < 10000; i++) assert completer.take().get();
    }

    public final static Object[][] base64KeyMessageDigest = {
            {Base64.decode("CwsLCwsLCwsLCwsLCwsLCwsLCws="), "Hi There", "thcxhlUFcmTii8C2+zeMjvFGvgA="},
            {Base64.decode("SmVmZQ=="), "what do ya want for nothing?", "7/zfauXrL6LSdBbV8YTfnCWafHk="},
            {Base64.decode("DAwMDAwMDAwMDAwMDAwMDAwMDAw="), "Test With Truncation", "TBoDQktV4H/n8nvh1Yu5MkqaWgQ="},
            {Base64.decode("qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqo="), "Test Using Larger Than Block-Size Key - Hash Key First", "qkrl4VJy0A6VcFY3zoo7Ve1AIRI="},
            {Base64.decode("qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqo="), "Test Using Larger Than Block-Size Key and Larger Than One Block-Size Data", "6OmdD0UjfXhta7qnllx4CLv/GpE="}
    };

    @DataProvider(name = "hmacsha1")
    public Object[][] createData1() {
        return base64KeyMessageDigest;
    }


    @Test(dataProvider = "hmacsha1")
    public void testBouncyCastleDigest(byte[] key, String message, String base64Digest) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException {
        String b64 = S3Utils.digest(message, key);
        assert b64.equals(base64Digest);
    }

}