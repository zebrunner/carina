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
package com.qaprosoft.carina.core.foundation.http.filters;

import com.jayway.restassured.filter.Filter;
import com.jayway.restassured.filter.FilterContext;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.FilterableRequestSpecification;
import com.jayway.restassured.specification.FilterableResponseSpecification;
import com.qaprosoft.carina.core.foundation.log.TestLogHelper;
import com.qaprosoft.carina.core.foundation.utils.XmlFormatter;

/*
 * Response log filter for formatting HTTP response logs.
 * 
 * @author Alex Khursevich
 */
public class ResponseLogFilter implements Filter
{
	private TestLogHelper logCntxt;

	public ResponseLogFilter(TestLogHelper logCntxt)
	{
		this.logCntxt = logCntxt;
	}
	
	@Override
	public Response filter(FilterableRequestSpecification rq, FilterableResponseSpecification rs, FilterContext cntxt)
	{
		final Response response = cntxt.next(rq, rs);
		if (logCntxt != null)
		{
			logCntxt.log("********** Response **********");
			logCntxt.log(response.getStatusLine());
			logCntxt.log(XmlFormatter.prettyPrint(response.asString()));
			logCntxt.log("******************************");
		}
		return response;
	}
}