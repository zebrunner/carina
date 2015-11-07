/*
 * Copyright 2013-2015 QAPROSOFT (http://qaprosoft.com/).
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
package com.qaprosoft.carina.core.foundation.utils.amazon;

import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.Configuration.S3Mode;

/*
 * Amazon S3
 * 
 */
public class AmazonS3Manager
{
	private static final Logger LOGGER = Logger.getLogger(AmazonS3Manager.class);
	private static IAmazonS3Manager s3manager;
	private static boolean isInitialized = false;
	
	static
	{
		try
		{
			if (!Configuration.getS3Mode().equals(S3Mode.OFF)) {
				s3manager = (IAmazonS3Manager) Class.forName(Configuration.get(Parameter.S3_MANAGER)).newInstance();
				isInitialized = true;
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			LOGGER.error("S3 Manager utility not initialized for '" + Configuration.get(Parameter.S3_MANAGER) + "':" + e.getMessage());
		}
	}

	public synchronized static void put(String bucketName, String key, String filePath)
	{
		
		if(isInitialized)
		{
			try
			{
				s3manager.put(bucketName, key, filePath);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				LOGGER.error("S3 Manager 'put' not performed: " + e.getMessage());
			}
		}
	}
	
	public synchronized static void delete(String bucketName, String key)
	{
		
		if(isInitialized)
		{
			try
			{
				s3manager.delete(bucketName, key);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				LOGGER.error("S3 Manager 'delete' not performed: " + e.getMessage());
			}
		}
	}

}
