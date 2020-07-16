package org.d11.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class LoginResponse {

    @JsonProperty("authentication_token")
    private String authenticationToken;

    public boolean isValid() {
        return this.authenticationToken != null && !this.authenticationToken.isEmpty();
    }
    
}
