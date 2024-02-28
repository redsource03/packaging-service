package com.globe.gcash.packaging.model.response;

import lombok.Data;

@Data
public class GenericErrorResponse {
    public GenericErrorResponse (int status, String errorCode, String errorMessage) {
        this.status = status;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    private int status;
    private String errorCode;
    private String errorMessage;
}
