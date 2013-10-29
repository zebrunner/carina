/*
 * Copyright 2013 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qaprosoft.carina.core.foundation.crypto;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import com.qaprosoft.carina.core.foundation.utils.R;

public class SecretKeyManager
{
	private static final String DEFAULT_ALGORITHM = R.CONFIG.get("crypto_key_type");
	private static final int DEFAULT_SIZE = R.CONFIG.getInt("crypto_key_size");
	
	private static SecretKey generateKey(String algorithm, int size) throws NoSuchAlgorithmException 
	{
		KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
	    keyGenerator.init(size);
	    SecretKey key = keyGenerator.generateKey();
	    return key; 
	}
	
	public static SecretKey generateKey() throws NoSuchAlgorithmException 
	{
	    return generateKey(DEFAULT_ALGORITHM, DEFAULT_SIZE); 
	}   
	
	public static void saveKey(SecretKey key, File file) throws IOException  {
	    byte[] encoded = key.getEncoded();
	    FileUtils.writeByteArrayToFile(file, Base64.encodeBase64(encoded));
	}
	
	public static SecretKey loadKey(File file) throws IOException {
	    SecretKey key = new SecretKeySpec(Base64.decodeBase64(FileUtils.readFileToByteArray(file)), DEFAULT_ALGORITHM);
	    return key;
	}
}
