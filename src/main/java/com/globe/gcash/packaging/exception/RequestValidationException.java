package com.globe.gcash.packaging.exception;

import java.io.Serial;
import java.io.Serializable;

public class RequestValidationException extends RuntimeException implements Serializable {
    public RequestValidationException(String message) {
        super(message);
    }

    @Serial
    private static final long serialVersionUID = 1L;
}
