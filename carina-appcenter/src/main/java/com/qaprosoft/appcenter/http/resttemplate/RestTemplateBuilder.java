/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
 *******************************************************************************/
package com.qaprosoft.appcenter.http.resttemplate;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.qaprosoft.appcenter.http.resttemplate.ssl.DisabledSslClientHttpRequestFactory;

/**
 * Created by mk on 6/30/15.
 */
public class RestTemplateBuilder {

    protected RestTemplate restTemplate = new RestTemplate();

    protected List<HttpMessageConverter<?>> httpMessageConverters = new ArrayList<HttpMessageConverter<?>>();

    protected boolean isDisableSslChecking = false;

    protected boolean isUseDefaultJsonMessageConverter = true;

    protected boolean isUseBasicAuth = false;

    protected String basicAuthUsername;

    protected String basicAuthPassword;

    protected RestTemplateBuilder() {
    }

    public static RestTemplateBuilder newInstance() {
        return new RestTemplateBuilder();
    }

    public RestTemplateBuilder withMessageConverter(
            HttpMessageConverter<?> messageConverter) {
        this.httpMessageConverters.add(messageConverter);
        return this;
    }

    public RestTemplateBuilder withDisabledSslChecking() {
        this.isDisableSslChecking = true;
        return this;
    }

    public RestTemplateBuilder withSpecificJsonMessageConverter() {
        isUseDefaultJsonMessageConverter = false;

        AbstractHttpMessageConverter<?> jsonMessageConverter = new MappingJackson2HttpMessageConverter(
                Jackson2ObjectMapperBuilder
                        .json()
                        .featuresToEnable(
                                DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                                DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                        .build());
        jsonMessageConverter.setSupportedMediaTypes(Lists.newArrayList(
                MediaType.TEXT_HTML, MediaType.TEXT_PLAIN,
                MediaType.APPLICATION_JSON));

        withMessageConverter(jsonMessageConverter);

        return this;
    }

    /**
     * http://stackoverflow.com/questions/27603782/java-spring-resttemplate-character-encoding
     * 
     * @return RestTemplateBuilder
     */
    public RestTemplateBuilder withUtf8EncodingMessageConverter() {
        withMessageConverter(new StringHttpMessageConverter(Charset.forName("UTF-8")));

        return this;
    }

    public RestTemplateBuilder withBasicAuthentication(String username, String password) {
        isUseBasicAuth = true;
        basicAuthUsername = username;
        basicAuthPassword = password;

        return this;
    }

    public RestTemplate build() {

        if (!isUseDefaultJsonMessageConverter) {

            HttpMessageConverter<?> httpMessageConverter = Iterables.tryFind(
                    restTemplate.getMessageConverters(),
                    new Predicate<HttpMessageConverter<?>>() {
                        @Override
                        public boolean apply(HttpMessageConverter<?> input) {
                            return input instanceof MappingJackson2HttpMessageConverter;
                        }
                    }).orNull();

            restTemplate.getMessageConverters().remove(httpMessageConverter);
        }

        restTemplate.getMessageConverters().addAll(httpMessageConverters);

        if (isDisableSslChecking) {
            restTemplate
                    .setRequestFactory(new DisabledSslClientHttpRequestFactory());
        }

        if (isUseBasicAuth) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(basicAuthUsername, basicAuthPassword));

            HttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).build();

            restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
        }

        return restTemplate;
    }
}
