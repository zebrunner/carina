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
package com.qaprosoft.carina.core.foundation.report;

/**
 * Test result type.
 * 
 * @author: Aliaksei_Khursevich (hursevich@gmail.com)
 */
public enum TestResultType
{
	PASS("PASS"),
	FAIL("FAIL"),
	SERVICE("SERVICE"),
	SKIP("SKIP");

	private String result;
	
	int passed;
	int failed;
	int service;
	int skipped;

	TestResultType(String result)
	{
		this.result = result;
	}
	
	TestResultType(String result, int passed, int failed, int service, int skipped)
	{
		this.result = result;
		this.passed = passed;
		this.failed = failed;
		this.service = service;
		this.skipped = skipped;
	}

	public String getName()
	{
		return result;
	}
	
	public int getPassed() {
		return passed;
	}

	public void setPassed(int passed) {
		this.passed = passed;
	}

	public int getFailed() {
		return failed;
	}

	public void setFailed(int failed) {
		this.failed = failed;
	}

	public int getService() {
		return service;
	}

	public void setService(int service) {
		this.service = service;
	}
	
	public int getSkipped() {
		return skipped;
	}

	public void setSkipped(int skipped) {
		this.skipped = skipped;
	}

}
