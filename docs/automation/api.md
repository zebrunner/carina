### Introduction
Rest API testing is a vital part of integration testing process, it may be used separately or in complex with web, mobile or DB testing. General process may be described by the following steps:

1. Compile HTTP request with required meta data
2. Send prepared data to required server endpoint
3. Validate HTTP status and response data
4. Extract some response data for the next requests
5. Build call to next (or same) endpoint using (or not using) data from previous response

Schema below demonstrates the sequence:
![API flow](../img/api/api-flow-new.png)

From that perspective we decided to use the following instruments:

* Rest-assured - "Testing and validation of REST services in Java is harder than in dynamic languages such as Ruby and Groovy. REST
Assured brings the simplicity of using these languages into the Java domain."
* Freemarker - "Apache FreeMarker is a template engine: a Java library to generate text output (HTML web pages, e-mails, configuration
files, source code, etc.) based on templates and changing data."
* JsonPath - library for extracting data from JSON body
* JsonAssert - library for comparing of actual JSON body with expected one
* Json-schema-validator - library for validating of JSON body for matching to json schema

### Example of test implementation
Let's create automated test for next call: POST https://jsonplaceholder.typicode.com/users request with requst body
```
[
    {
        "id": 1,
        "name": "SOME_NAME",
        "username": "SOME_USERNAME",
        "email": "Sincere@april.biz",
        "address": {
            "street": "Kulas Light",
            "suite": "Apt. 556",
            "city": "Gwenborough",
            "zipcode": "92998-3874",
            "geo": {
                "lat": "-37.3159",
                "lng": "81.1496"
            }
        },
        "phone": "1-770-736-8031 x56442",
        "website": "hildegard.org",
        "company": {
            "name": "SOME_COMPANY_NAME",
            "catchPhrase": "Multi-layered client-server neural-net",
            "bs": "harness real-time e-markets"
        }
    }
]
```
and response body same as request body

#### Definition of request and response templates
If we are going to send POST request we have to create request template with some placeholders that may be replaced by different arguments for different test flows. The best place to store these resources is src/test/resources/api package, try to keep REST hierarchy in package structure for better maintenance and visibility:
![API flow](../img/api/resources-structure.png)
Request (rq.json) and response (rs.json) templates have some placeholders that will be populated from the tests later on:
![API flow](../img/api/rq-example.png)
![API flow](../img/api/rs-example.png)
While user.properties contains some default value which may be replaced later:
![API flow](../img/api/props-example.png)

#### REST service call domain object
Now we are ready to create REST service domain object which will be used to interact with web service and perform additional response validations. Our domain object is located in /carina-demo/src/main/java/com/qaprosoft/carina/demo/api, make sure that it extends AbstractApiMethodV2 and triggers base class constructor for initialization. In general case you will specify path to request and response templates along with default properties files (all of them were created in previous step). Also we replace URL placeholder to set appropriate environment.
```
package com.qaprosoft.carina.demo.api;

import com.qaprosoft.carina.core.foundation.api.AbstractApiMethodV2;
import com.qaprosoft.carina.core.foundation.utils.Configuration;

public class PostUserMethod extends AbstractApiMethodV2 {
    public PostUserMethod() {
        super("api/users/_post/rq.json", "api/users/_post/rs.json", "api/users/user.properties");
        replaceUrlPlaceholder("base_url", Configuration.getEnvArg("api_url"));
    }
}
```

#### HTTP method and path
The last step before test implementation itself is association of domain object class and required HTTP method and path.
It should be defined in /carina-demo/src/main/resources/_api.properties file, key should be equal to domain class name, value has the following pattern {http_method}:{http_path}. HTTP path may contain placeholders, HTTP method should be one of the following variants: GET, POST, PUT, UPDATE, DELETE.
```
#=====================================================#
#=================== API methods  ====================#
#=====================================================#
GetUserMethods=GET:${base_url}/users
PostUserMethod=POST:${base_url}/users
DeleteUserMethod=DELETE:${base_url}/users/1
PutPostsMethod=PUT:${base_url}/posts/1
PatchPostsMethod=PATCH:${base_url}/posts/1
```

#### API test
API test is general TestNG test, class should extend APITest, in our case test extend it over AbstractTest that encapsulates some test data and login method. Test is located in /carina-demo/src/test/java/com/qaprosoft/carina/demo.
```
package com.qaprosoft.carina.demo;

import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testng.annotations.Test;

import com.qaprosoft.apitools.validation.JsonCompareKeywords;
import com.qaprosoft.carina.core.foundation.AbstractTest;
import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;
import com.qaprosoft.carina.core.foundation.utils.ownership.MethodOwner;
import com.qaprosoft.carina.demo.api.DeleteUserMethod;
import com.qaprosoft.carina.demo.api.GetUserMethods;
import com.qaprosoft.carina.demo.api.PostUserMethod;

public class APISampleTest extends AbstractTest {
    @Test(description = "JIRA#DEMO-0001")
    @MethodOwner(owner = "qpsdemo")
    public void testCreateUser() throws Exception {
        PostUserMethod api = new PostUserMethod();
        api.expectResponseStatus(HttpResponseStatusType.CREATED_201);
        api.callAPI();
        api.validateResponse();
    }

    @Test(description = "JIRA#DEMO-0002")
    @MethodOwner(owner = "qpsdemo")
    public void testCreateUserMissingSomeFields() throws Exception {
        PostUserMethod api = new PostUserMethod();
        api.getProperties().remove("name");
        api.getProperties().remove("username");
        api.expectResponseStatus(HttpResponseStatusType.CREATED_201);
        api.callAPI();
        api.validateResponse();
    }

    @Test(description = "JIRA#DEMO-0003")
    @MethodOwner(owner = "qpsdemo")
    public void testGetUsers() {
        GetUserMethods getUsersMethods = new GetUserMethods();
        getUsersMethods.expectResponseStatus(HttpResponseStatusType.OK_200);
        getUsersMethods.callAPI();
        getUsersMethods.validateResponse(JSONCompareMode.STRICT, JsonCompareKeywords.ARRAY_CONTAINS.getKey());
        getUsersMethods.validateResponseAgainstJSONSchema("api/users/_get/rs.schema");
    }
}
```

#### Test steps once again
1. Create REST call object
2. Specify properties for request/response placeholder
3. Add headers if required
4. Specify expected HTTP status
5. Call API
6. Validate response by template or parse some data by JSON path
7. Make further calls using data from previos call if needed

### Useful features
Framework contains list of useful feature for requests building and for responses validation. That makes easier support of such tests and and at the same time minimizes amount of test data.

#### Wildcards
In some cases you may need to generate data in request to make request data unique. The best way of doing this is to use wildcards for data generation:
```
{
    "username": "generate_word(10)",          // Will generate random alphanumeric string with 10 characters
    "zip": "generate_number(6)",              // Will generate random number with 6 digits
    "birthday": "generate_date(yyyy-MM-dd;0)" // Will generate current date (first arg is date format, second is delta in days from now)
}
```
Other option is to specify placeholder in request template and then pass some generated value directly from the test method.

Another useful way of wildcards usage is response validation. In some cases you may need to skip some values or validate by regex:
```
{
    "id": "skip",                                    // Will skip actual value validation and just verify id key presence
    "signup_date": "regex:\\d{4}-\\d{2}-\\d{2}",     // Will validate date value by specified regex
}
```

#### Validation against JSON schema
When you need to validate response structure regardless of the actual values you may use validation by JSON schema. In this case you need actual response from service, let's say we have the following:
```
{
    "email": "test@domain.com",
    "firstName": "SOME FIRST NAME",
    "id": 11111
}
```
Now we need to generate schema (you may use any generator you like for example https://jsonschema.net/).
IMPORTANT: For now schemas of version draft03 and draft04 are supported only. Please use appropriate generator (e.g.  https://www.liquid-technologies.com/online-json-to-schema-converter)
In tool like this you need to provide original JSON from response then choose some schema options (allow additional properties in objects, mark current object properties as required, hard-code some expected values, ect) and then generate the schema. Copy-paste generated schema into test resources and you're ready to use it in the test.
![API flow](../img/api/schema-generator.png)
Make sure that you change all flags required to true, after that create new file in resources and place into appropriate endpoint package:
```
{
    "type":"object",
    "$schema": "http://json-schema.org/draft-03/schema",
    "id": "http://jsonschema.net",
    "required":true,
    "properties":{
        "email": {
            "type":"string",
            "id": "http://jsonschema.net/email",
            "required":true
        },
        "firstName": {
            "type":"string",
            "id": "http://jsonschema.net/firstName",
            "required":true
        },
        "id": {
            "type":"number",
            "id": "http://jsonschema.net/id",
            "required":true
        }
    }
}
```
And finally we call JSON validation from Java test as following:
```
@Test
public void testCheckJSONSchema()
{
    PostUserLoginMethod api = new PostUserLoginMethod();
    api.expectResponseStatus(HttpResponseStatusType.OK_200);
    api.callAPI();
    api.validateResponseAgainstJSONSchema("api/testdata/users/login/_post/rs.schema");
}
```

#### Building of requests with array
There are couple options for building request with array of items provided by framework:
1. First one use hardcoded placholders for changeable variables.
```
{
   "name": "${name}",
   "description": "${description}",
   "label": "${label}",
   "taskTypes": [
      {
         "name": "${task_name_1}",
         "description": "${task_description_1}"
      }
      <#if task_name_2?exists || task_description_2?exists>,
      {
         "name": "${task_name_2}",
         "description": "${task_description_2}"
      }
      </#if>
      <#if task_name_3?exists || task_description_3?exists>,
      {
         "name": "${task_name_3}",
         "description": "${task_description_3}"
      }
      </#if>
   ]
}
```
As you see this structure is pretty flexible. If you need 2 taskTypes items then you need to declare at least task_name_2 or task_description_2 property. If you need 3 items in addition to that you need to declare task_name_3 or task_description_3 property. Otherwise array will contain only 1 item.
For instance you need to build json which contains taskTypes array. Then template with placeholders will be following:
It's easy to extend such structure. You just need to add items with similar placeholders increasing their index.

2. Other approach is based on using Freemarker loop. Here is the template example for same JSON:
```
<#if task_name_1?exists>
    <#assign task_names = [task_name_1]>
    <#assign task_descriptions = [task_description_1]>
</#if>   
 
<#if task_name_2?exists>
    <#assign task_names = [task_name_1, task_name_2]>
    <#assign task_descriptions = [task_description_1, task_description_2]>
</#if>
 
<#if task_name_3?exists>
    <#assign task_names = [task_name_1, task_name_2, task_name_3]>
    <#assign task_descriptions = [task_description_1, task_description_2, task_description_3]>
</#if>
 
{
   "name": "${name}",
   "description": "${description}",
   "label": "${label}",
   "taskTypes": [
      <#list 0..task_names?size-1 as i>
      {
         "name": "${task_names[i]}",
         "description": "${task_descriptions[i]}"
      }
      <#if (i + 1) < task_names?size>,</#if>
      </#list>
   ]
}
```
This approach is useful when structure of array item is pretty complex. So it makes sense to specify item attributes only once doing it inside #list operation.
This approach also allows to choose amount of array items dynamically.
But note that you should specify all properties for each item so this view can be not used for negative tests when you need to miss some properties.

#### Validation of responses with array
Sometimes you could face situation when you need to validate presence of only one (or couple) item in JSON array ignoring rest items.
In such case you can use validation option ARRAY_CONTAINS.
Here is code sample:
```
JSONAssert.assertEquals(expectedRs, actualRs, new JsonKeywordsComparator(JSONCompareMode.STRICT,
                    JsonCompareKeywords.ARRAY_CONTAINS.getKey() + "content"));
```
Expected array:
```
{
    "totalElements": "skip",
    "pageNumber": "skip",
    "pageSize": "skip",
    "content": [
        {
            "id": skip,
            "brand": "skip",
            "clientName": "CLIENT 1"
        },
        {
            "id": "skip",
            "brand": "skip",
            "clientName": "CLIENT 2"
        }
    ]
}
```
And actual response:
```
{
    "totalElements": 1017,
    "pageNumber": 0,
    "pageSize": 100,
    "content": [
        {
            "id": 11111,
            "brand": "test",
            "clientName": "CLIENT 1"
        },
        {
            "id": 22222,
            "brand": "test",
            "clientName": "CLIENT 2"
        },
        {
            "id": 3333,
            "brand": "test",
            "clientName": "CLIENT 3"
        },
        {
            "id": 4444,
            "brand": "test",
            "clientName": "CLIENT 4"
        }
    ]
}
```

### Deserialization of JSON
Sometimes you may need to transform your json response to POJO. It maybe useful if you need to validate your response using data from database as expected data.
For that purposes it's better to use Jackson libraries that are already included in carina framework.
For that you have to prepare domain class based on your json structure. Some online resources provide such opportunities. For instance https://timboudreau.com/blog/json/read
For example we need to deserialize array of Clients from json. Example of required domain object will be:
```
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
public final class Clients
{
    public final Client clients[];
    @JsonCreator
    public Clients(@JsonProperty("clients") Client[] clients)
    {
        this.clients = clients;
    }
    public static final class Client
    {
        public final long id;
        public final String brand;
        public final String clientName;
        ......
        @JsonCreator
        public Client(@JsonProperty("id") long id, @JsonProperty("brand") String brand, @JsonProperty("clientName") String clientName,.....)
        {
            this.id = id;
            this.brand = brand;
            this.clientName = clientName;
            .........
        }
    }
    public Client[] getClients()
    {
        return clients;
    }
}
```
Pay attention that POJO fields names could differ from json properties. In this case @JsonProperty annotation can be used for mapping.
Example of deserialization code:
```
GetClientsMethod getClientsMethod = new GetClientsMethod("11111");
getClientsMethod.expectResponseStatus(HttpResponseStatusType.OK_200);
String rs = getClientsMethod.callAPI().asString();
ObjectMapper mapper = new ObjectMapper();
Clients clients = mapper.readValue(rs, Clients.class);
```
Then you can use POJO object for any kind of validation or for easy retrieving of required properties.
