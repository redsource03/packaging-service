package com.globe.gcash.packaging.exception;

import java.io.Serial;
import java.io.Serializable;

public class ApiClientException extends RuntimeException implements Serializable {

    public ApiClientException(String message) {
        super(message);
    }
    @Serial
    private static final long serialVersionUID = 1L;
}
