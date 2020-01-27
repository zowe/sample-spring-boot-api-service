package org.zowe.sample.apiservice.security;

import lombok.Data;

@Data
public class AppResponse {

    private String status;
    private int statusCode;
    private String message;

    public AppResponse(String status, int statusCode, String message) {
        this.status = status;
        this.statusCode = statusCode;
        this.message = message;
    }

}

