package com.qaprosoft.carina.core.foundation.api.log;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.hamcrest.Matcher;

import io.restassured.builder.ResponseBuilder;
import io.restassured.filter.FilterContext;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.internal.RestAssuredResponseImpl;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class CarinaResponseLoggingFilter extends ResponseLoggingFilter {

    private final PrintStream stream;
    private final Matcher<?> matcher;
    private final LogDetail logDetail;
    private final boolean shouldPrettyPrint;
    private final Set<String> blacklistedHeaders;

    public CarinaResponseLoggingFilter(LogDetail logDetail, boolean prettyPrint, PrintStream stream, Matcher<? super Integer> matcher,
            Set<String> blacklistedHeaders) {
        Validate.notNull(logDetail, "Log details cannot be null");
        Validate.notNull(stream, "Print stream cannot be null");
        Validate.notNull(matcher, "Matcher cannot be null");
        Validate.notNull(blacklistedHeaders, "Blacklisted headers cannot be null");
        if (logDetail == LogDetail.PARAMS || logDetail == LogDetail.URI || logDetail == LogDetail.METHOD) {
            throw new IllegalArgumentException(String.format("%s is not a valid %s for a response.", logDetail, LogDetail.class.getSimpleName()));
        }
        this.shouldPrettyPrint = prettyPrint;
        this.logDetail = logDetail;
        this.stream = stream;
        this.matcher = matcher;
        this.blacklistedHeaders = new HashSet<>(blacklistedHeaders);
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        Response response = ctx.next(requestSpec, responseSpec);
        final int statusCode = response.statusCode();
        if (matcher.matches(statusCode)) {
            CarinaResponsePrinter.print(response, response, stream, logDetail, shouldPrettyPrint, blacklistedHeaders);
            final byte[] responseBody;
            if (logDetail == LogDetail.BODY || logDetail == LogDetail.ALL) {
                responseBody = response.asByteArray();
            } else {
                responseBody = null;
            }
            response = cloneResponseIfNeeded(response, responseBody);
        }

        return response;
    }

    /*
     * If body expectations are defined we need to return a new Response otherwise the stream
     * has been closed due to the logging.
     */
    private Response cloneResponseIfNeeded(Response response, byte[] responseAsString) {
        if (responseAsString != null && response instanceof RestAssuredResponseImpl && !((RestAssuredResponseImpl) response).getHasExpectations()) {
            final Response build = new ResponseBuilder().clone(response).setBody(responseAsString).build();
            ((RestAssuredResponseImpl) build).setHasExpectations(true);
            return build;
        }
        return response;
    }

}
