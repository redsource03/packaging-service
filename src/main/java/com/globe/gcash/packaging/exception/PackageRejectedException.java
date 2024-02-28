package com.globe.gcash.packaging.exception;

import java.io.Serial;
import java.io.Serializable;

public class PackageRejectedException extends RuntimeException implements Serializable {
    public PackageRejectedException(String message) {
        super(message);
    }

    @Serial
    private static final long serialVersionUID = 1L;
}
