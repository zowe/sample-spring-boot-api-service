package org.zowe.commons.spring.token;

import lombok.Data;

@Data
public class TokenResponse {

    private String jwtToken;

    public TokenResponse(String status) {
        this.jwtToken = status;
    }

}
