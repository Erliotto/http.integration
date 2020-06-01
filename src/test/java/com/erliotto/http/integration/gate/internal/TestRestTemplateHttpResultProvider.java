package com.erliotto.http.integration.gate.internal;

import com.erliotto.http.integration.gate.HttpResultProvider;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public final class TestRestTemplateHttpResultProvider implements HttpResultProvider {
    private final TestRestTemplate testRestTemplate;

    public TestRestTemplateHttpResultProvider(TestRestTemplate testRestTemplate) {
        this.testRestTemplate = testRestTemplate;
    }

    @Override
    public Result call(HttpMethod httpMethod, String url, HttpHeaders httpHeaders, Object payload) {
        final Map<String, ?> urlVariables = new HashMap<>();
        final Class respType = String.class;

        final ResponseEntity<String> responseEntity = testRestTemplate.exchange(url, httpMethod, new HttpEntity(payload), respType, urlVariables);
        return new HttpResultProvider.Result(responseEntity.getStatusCode(), responseEntity.getBody());
    }
}
