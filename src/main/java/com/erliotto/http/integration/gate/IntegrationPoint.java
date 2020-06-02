package com.erliotto.http.integration.gate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class IntegrationPoint<TResponse extends HttpStatusHolder> {
    private final HttpResultProvider httpResultProvider;
    private final ObjectMapper objectMapper;
    private final Map<HttpStatus, Value> responseDescriptors;

    private Value defaultValue;

    public IntegrationPoint(HttpResultProvider httpResultProvider, ObjectMapper objectMapper) {
        this.httpResultProvider = httpResultProvider;
        this.objectMapper = objectMapper;
        this.responseDescriptors = new HashMap<>();
    }

    public TResponse call(HttpMethod httpMethod, String url, HttpHeaders httpHeaders, Object payload) throws JsonProcessingException {
        final HttpResultProvider.Result httpResult = httpResultProvider.call(httpMethod, url, httpHeaders, payload);
        if (httpResult == null) {
            return null;
        }

        return dispatchResponse(httpResult.httpStatus, httpResult.body);
    }

    public IntegrationPoint<TResponse> register(HttpStatus httpStatus, Class<? extends TResponse> responseClass) {
        check(responseClass);
        checkHttpStatus(httpStatus);

        this.responseDescriptors.put(httpStatus, createValue(responseClass));
        return this;
    }

    public <TRawResponse> IntegrationPoint<TResponse> register(HttpStatus httpStatus,
                                                               Class<TRawResponse> rawResponseClass,
                                                               Function<TRawResponse, TResponse> responseMapper) {
        check(rawResponseClass, responseMapper);
        checkHttpStatus(httpStatus);

        this.responseDescriptors.put(httpStatus, createValue(rawResponseClass, responseMapper));
        return this;
    }

    public IntegrationPoint<TResponse> registerDefault(Class<? extends TResponse> responseClass) {
        check(responseClass);
        checkDefault();

        this.defaultValue = createValue(responseClass);
        return this;
    }

    public <TRawResponse> IntegrationPoint<TResponse> registerDefault(Class<TRawResponse> rawResponseClass,
                                                                      Function<TRawResponse, TResponse> responseMapper) {
        check(rawResponseClass, responseMapper);
        checkDefault();

        this.defaultValue = createValue(rawResponseClass, responseMapper);
        return this;
    }

    private void check(Class<? extends TResponse> responseClass) {
        if (responseClass == null) {
            throw new IllegalArgumentException("responseClass");
        }
    }

    private <TRawResponse> void check(Class<TRawResponse> rawResponseClass, Function<TRawResponse, TResponse> responseMapper) {
        if (rawResponseClass == null) {
            throw new IllegalArgumentException("rawResponseClass");
        }

        if (responseMapper == null) {
            throw new IllegalArgumentException("responseMapper");
        }
    }

    private void checkHttpStatus(HttpStatus httpStatus) {
        if (this.responseDescriptors.containsKey(httpStatus)) {
            throw new IllegalArgumentException(String.format("already have status: %s", httpStatus));
        }
    }

    private void checkDefault() {
        if (this.defaultValue != null) {
            throw new IllegalArgumentException("already have default");
        }
    }

    private Value createValue(Class<? extends TResponse> responseClass) {
        return new Value(responseClass);
    }

    private <TRawResponse> Value createValue(Class<TRawResponse> rawResponseClass, Function<TRawResponse, TResponse> responseMapper) {
        return new Value(rawResponseClass, x -> responseMapper.apply((TRawResponse) x));
    }

    private TResponse dispatchResponse(HttpStatus key, String rawResponse) throws JsonProcessingException {
        final Value value = this.responseDescriptors.get(key);

        if (value != null) {
            return acceptResponse(key, value, rawResponse);
        }

        if (this.defaultValue != null) {
            return acceptResponse(key, this.defaultValue, rawResponse);
        }

        return null;
    }

    private TResponse acceptResponse(HttpStatus key, Value value, String rawResponse) throws JsonProcessingException {
        final Object rawResponseValue = value.rawResponseClass != String.class
                ? objectMapper.readValue(rawResponse, value.rawResponseClass)
                : rawResponse;

        final TResponse externalServiceResponse = value.responseMapper == null
                ? (TResponse) rawResponseValue
                : (TResponse) value.responseMapper.apply(rawResponseValue);

        externalServiceResponse.setHttpStatus(key);
        return externalServiceResponse;
    }

    private static class Value {
        private final Class<?> rawResponseClass;
        private final Function<Object, ?> responseMapper;

        private Value(Class<?> rawResponseClass) {
            this.rawResponseClass = rawResponseClass;
            this.responseMapper = null;
        }

        private Value(Class<?> rawResponseClass, Function<Object, ?> responseMapper) {
            this.rawResponseClass = rawResponseClass;
            this.responseMapper = responseMapper;
        }
    }
}
