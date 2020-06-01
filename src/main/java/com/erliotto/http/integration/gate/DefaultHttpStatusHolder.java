package com.erliotto.http.integration.gate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;

public abstract class DefaultHttpStatusHolder implements HttpStatusHolder {
    @JsonIgnore
    private HttpStatus responseStatus;

    @JsonIgnore
    @Override
    public void setHttpStatus(HttpStatus httpStatus) {
        this.responseStatus = httpStatus;
    }

    @JsonIgnore
    @Override
    public HttpStatus getHttpStatus() {
        return responseStatus;
    }

    @JsonIgnore
    public boolean isSuccess() {
        return responseStatus.is2xxSuccessful();
    }
}
