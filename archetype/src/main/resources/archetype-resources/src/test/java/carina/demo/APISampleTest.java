#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import java.lang.invoke.MethodHandles;

import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.qaprosoft.apitools.validation.JsonCompareKeywords;
import com.qaprosoft.carina.core.foundation.IAbstractTest;
import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;
import com.qaprosoft.carina.core.foundation.utils.ownership.MethodOwner;
import com.qaprosoft.carina.core.foundation.utils.tag.Priority;
import com.qaprosoft.carina.core.foundation.utils.tag.TestPriority;
import ${package}.carina.demo.api.DeleteUserMethod;
import ${package}.carina.demo.api.GetUserMethods;
import ${package}.carina.demo.api.PostUserMethod;

/**
 * This sample shows how create REST API tests.
 *
 * @author qpsdemo
 */
public class APISampleTest implements IAbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    @Test()
    @MethodOwner(owner = "qpsdemo")
    public void testCreateUser() throws Exception {
        LOGGER.info("test");
        setCases("4555,54545");
        PostUserMethod api = new PostUserMethod();
        api.expectResponseStatus(HttpResponseStatusType.CREATED_201);
        api.callAPI();
        api.validateResponse();
    }

    @Test()
    @MethodOwner(owner = "qpsdemo")
    public void testCreateUserMissingSomeFields() throws Exception {
        PostUserMethod api = new PostUserMethod();
        api.getProperties().remove("name");
        api.getProperties().remove("username");
        api.expectResponseStatus(HttpResponseStatusType.CREATED_201);
        api.callAPI();
        api.validateResponse();
    }

    @Test()
    @MethodOwner(owner = "qpsdemo")
    public void testGetUsers() {
        GetUserMethods getUsersMethods = new GetUserMethods();
        getUsersMethods.expectResponseStatus(HttpResponseStatusType.OK_200);
        getUsersMethods.callAPI();
        getUsersMethods.validateResponse(JSONCompareMode.STRICT, JsonCompareKeywords.ARRAY_CONTAINS.getKey());
        getUsersMethods.validateResponseAgainstSchema("api/users/_get/rs.schema");
    }

    @Test()
    @MethodOwner(owner = "qpsdemo")
    @TestPriority(Priority.P1)
    public void testDeleteUsers() {
        DeleteUserMethod deleteUserMethod = new DeleteUserMethod();
        deleteUserMethod.expectResponseStatus(HttpResponseStatusType.OK_200);
        deleteUserMethod.callAPI();
        deleteUserMethod.validateResponse();
    }

}
