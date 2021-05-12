package com.qaprosoft.carina.core.foundation.api.log;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.Validate;

import com.qaprosoft.carina.core.foundation.api.http.ContentTypeEnum;

import io.restassured.filter.FilterContext;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class CarinaRequestBodyLoggingFilter extends RequestLoggingFilter {

    private final PrintStream stream;
    private final boolean shouldPrettyPrint;
    private final Set<String> hiddenPaths;
    private final ContentTypeEnum contentType;

    public CarinaRequestBodyLoggingFilter(boolean shouldPrettyPrint, PrintStream stream, Set<String> hiddenPaths, ContentTypeEnum contentType) {
        Validate.notNull(stream, "Print stream cannot be null");
        this.stream = stream;
        this.shouldPrettyPrint = shouldPrettyPrint;
        this.hiddenPaths = new HashSet<>(hiddenPaths);
        this.contentType = contentType;
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        CarinaBodyPrinter.printRequestBody(requestSpec, stream, shouldPrettyPrint, hiddenPaths, contentType);
        return ctx.next(requestSpec, responseSpec);
    }

}
