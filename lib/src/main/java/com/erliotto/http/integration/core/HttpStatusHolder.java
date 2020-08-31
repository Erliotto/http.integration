package com.erliotto.http.integration.core;

import org.springframework.http.HttpStatus;

public interface HttpStatusHolder {
    void setHttpStatus(HttpStatus httpStatus);
    HttpStatus getHttpStatus();
}
