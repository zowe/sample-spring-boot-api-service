package org.zowe.commons.spring.token;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Represents the query JSON response with the token information
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryResponse {
    private String userId;
    private Date creation;
    private Date expiration;
}
