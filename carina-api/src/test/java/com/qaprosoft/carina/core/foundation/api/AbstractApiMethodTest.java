package com.qaprosoft.carina.core.foundation.api;

import com.qaprosoft.carina.core.foundation.api.ssl.PutDocMethod;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AbstractApiMethodTest {

    private final static String BODY_CONTENT = "{\"key\": \"value\"}";

    @Test
    public void testGetRequestBodyMethod() {
        PutDocMethod putDocMethod = new PutDocMethod();
        putDocMethod.setBodyContent(BODY_CONTENT);
        Assert.assertEquals(putDocMethod.getRequestBody(), BODY_CONTENT);
    }
}
