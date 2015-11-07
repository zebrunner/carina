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


public class DefaultAmazonS3Manager implements IAmazonS3Manager {

	@Override
	public void put(String bucketName, String key, String filePath) {
		// DO nothing by default
	}

	@Override
	public void delete(String bucketName, String key) {
		// DO nothing by default
	}

}