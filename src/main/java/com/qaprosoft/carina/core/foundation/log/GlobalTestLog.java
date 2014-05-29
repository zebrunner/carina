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
package com.qaprosoft.carina.core.foundation.log;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

/**
 * GlobalTestLog
 * 
 * @author Alex Khursevich
 */
public class GlobalTestLog
{
	public static final String KEY = "GLOBAL_TEST_LOG";
	//private ConcurrentHashMap<String, StringBuilder> log = new ConcurrentHashMap<String, StringBuilder>();
	private Map<String,StringBuilder> log = Collections.synchronizedMap(new HashMap<String,StringBuilder>());
	
	public GlobalTestLog()
	{
		log.put(Type.SOAP.toString(), new StringBuilder());
		log.put(Type.REST.toString(), new StringBuilder());
		log.put(Type.UI.toString(), new StringBuilder());
		log.put(Type.API.toString(), new StringBuilder());
		log.put(Type.COMMON.toString(), new StringBuilder());
	}
	
	public void log(Type type, String message)
	{
		log.get(type.toString()).append(message + "\r\n");
	}
	
	public String readLog(Type type)
	{
		return log.get(type.toString()).toString();
	}
	
	public enum Type
	{
		UI, SOAP, REST, API, COMMON;
	}
}
