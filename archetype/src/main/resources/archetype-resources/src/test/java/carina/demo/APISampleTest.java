#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * Copyright 2013-2019 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ${package}.carina.demo;

import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testng.annotations.Test;

import com.qaprosoft.apitools.validation.JsonCompareKeywords;
import com.qaprosoft.carina.core.foundation.AbstractTest;
import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;
import com.qaprosoft.carina.core.foundation.utils.ownership.MethodOwner;
import com.qaprosoft.carina.core.foundation.utils.tag.Priority;
import com.qaprosoft.carina.core.foundation.utils.tag.TestPriority;
import com.qaprosoft.carina.core.foundation.utils.tag.TestTag;
import ${package}.carina.demo.api.DeleteUserMethod;
import ${package}.carina.demo.api.GetUserMethods;
import ${package}.carina.demo.api.PostUserMethod;

/**
 * This sample shows how create REST API tests.
 *
 * @author qpsdemo
 */
public class APISampleTest extends AbstractTest {

	@Test(description = "JIRA${symbol_pound}DEMO-0001")
	@MethodOwner(owner = "qpsdemo")
	@TestPriority(Priority.P0)
	@TestTag(name = "area", value = "API0")
	@TestTag(name = "priority", value = "P0")
	@TestTag(name = "feature", value = "feature0")
	public void testCreateUser() throws Exception {
		PostUserMethod api = new PostUserMethod();
		api.expectResponseStatus(HttpResponseStatusType.CREATED_201);
		api.callAPI();
		api.validateResponse();
	}

	@Test(description = "JIRA${symbol_pound}DEMO-0002")
	@MethodOwner(owner = "qpsdemo")
	@TestTag(name = "area22", value = "API2")
	@TestTag(name = "specialization22", value = "negativ6")
	public void testCreateUserMissingSomeFields() throws Exception {
		PostUserMethod api = new PostUserMethod();
		api.getProperties().remove("name");
		api.getProperties().remove("username");
		api.expectResponseStatus(HttpResponseStatusType.CREATED_201);
		api.callAPI();
		api.validateResponse();
	}

	@Test(description = "JIRA${symbol_pound}DEMO-0003")
	@MethodOwner(owner = "qpsdemo")
	@TestPriority(Priority.P3)
	public void testGetUsers() {
		GetUserMethods getUsersMethods = new GetUserMethods();
		getUsersMethods.expectResponseStatus(HttpResponseStatusType.OK_200);
		getUsersMethods.callAPI();
		getUsersMethods.validateResponse(JSONCompareMode.STRICT, JsonCompareKeywords.ARRAY_CONTAINS.getKey());
		getUsersMethods.validateResponseAgainstJSONSchema("api/users/_get/rs.schema");
	}

	@Test(description = "JIRA${symbol_pound}DEMO-0004")
	@MethodOwner(owner = "qpsdemo")
	public void testDeleteUsers() {
		DeleteUserMethod deleteUserMethod = new DeleteUserMethod();
		deleteUserMethod.expectResponseStatus(HttpResponseStatusType.OK_200);
		deleteUserMethod.callAPI();
		deleteUserMethod.validateResponse();
	}

}
