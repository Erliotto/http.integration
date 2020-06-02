package com.erliotto.http.integration.core;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

public final class RestTemplateHttpResultProvider implements HttpResultProvider {
    private final RestTemplate restTemplate;

    public RestTemplateHttpResultProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Result call(HttpMethod httpMethod, String url, HttpHeaders httpHeaders, Object payload) {
        try {
            final ResponseEntity<String> responseEntity = restTemplate.exchange(url, httpMethod, new HttpEntity(payload, httpHeaders), String.class);
            if (responseEntity == null) {
                return null;
            }

            return new HttpResultProvider.Result(responseEntity.getStatusCode(), responseEntity.getBody());
        } catch (HttpStatusCodeException e) {
            return new HttpResultProvider.Result(e.getStatusCode(), e.getResponseBodyAsString());
        }
    }
}
