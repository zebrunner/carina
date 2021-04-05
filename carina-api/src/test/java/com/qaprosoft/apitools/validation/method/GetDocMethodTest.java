package com.qaprosoft.apitools.validation.method;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class GetDocMethodTest {

    @Test
    public void testGetDocsMethodWithoutContentType() throws IOException {
        String actualJsonData = Files.lines(Path.of("src/test/resources/validation/array/duplicate/array_act.json"))
                .collect(Collectors.joining("\n"));
        MockServer server = new MockServer();
        server.start();
        server.createResponse("/docs", actualJsonData);
        GetDocMethod getDocMethod = new GetDocMethod();
        getDocMethod.callAPI();
        getDocMethod.validateResponse();
        server.stop();
    }
}
