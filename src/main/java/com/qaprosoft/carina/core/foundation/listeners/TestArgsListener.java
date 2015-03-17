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
package com.qaprosoft.carina.core.foundation.listeners;

import java.util.Map;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import com.qaprosoft.carina.core.foundation.utils.ParameterGenerator;

/*
 * Test arguments listener is responsible for processing test parameters with wildcards.
 * 
 * @author Alex Khursevich
 */
public class TestArgsListener extends TestListenerAdapter
{
	@Override
	public void onTestStart(ITestResult result)
	{
		if (result != null && result.getParameters() != null)
		{
			for (int i = 0; i < result.getParameters().length; i++)
			{
				if (result.getParameters()[i] instanceof String)
				{
					result.getParameters()[i] = ParameterGenerator.process(result.getParameters()[i].toString());
				}
				
				if (result.getParameters()[i] instanceof Map)
				{
					@SuppressWarnings("unchecked")
					Map<String, String> dynamicAgrs = (Map<String, String>) result.getParameters()[i];
					for (Map.Entry<String, String> entry : dynamicAgrs.entrySet()) {
						Object param = ParameterGenerator.process(entry.getValue());
						if (param != null)
							dynamicAgrs.put(entry.getKey(), param.toString());
						else
							dynamicAgrs.put(entry.getKey(), null);
					}
				}
			}
		}
	}
}
