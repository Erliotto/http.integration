package com.erliotto.http.integration.core;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

public interface HttpResultProvider {
    final class Result {
        public final HttpStatus httpStatus;
        public final String body;

        public Result(HttpStatus httpStatus, String body) {
            this.httpStatus = httpStatus;
            this.body = body;
        }
    }

    Result call(HttpMethod httpMethod, String url, HttpHeaders httpHeaders, Object payload);
}
