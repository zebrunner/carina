package com.qaprosoft.carina.core.foundation.api.annotation;

import io.restassured.internal.RequestSpecificationImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ApiMethodAnnotationTest {

    @Test
    public void testEndpoint() {
        ApiMethodWAnnotation m = new ApiMethodWAnnotation();
        Assert.assertEquals(m.getMethodPath(), "http://test.api.com", "Method path from annotation not as expected");
    }

    @Test
    public void testContentType() {
        ApiMethodWAnnotation m = new ApiMethodWAnnotation();
        Assert.assertEquals(((RequestSpecificationImpl)m.getRequest()).getContentType(), "application/xml",
                "Content type from annotation not as expected");
    }

}
