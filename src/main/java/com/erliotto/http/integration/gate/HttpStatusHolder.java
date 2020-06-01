package com.erliotto.http.integration.gate;

import org.springframework.http.HttpStatus;

public interface HttpStatusHolder {
    void setHttpStatus(HttpStatus httpStatus);
    HttpStatus getHttpStatus();
}
