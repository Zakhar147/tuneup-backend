package com.tuneup.backend.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse {
    private String accestoken;
    private String type = "Bearer";
    private String refreshToken;
    private Long id;
    private String username;
    private String email;

    public JwtResponse(String token, String refreshToken, Long id, String username, String email) {
        this.accestoken = token;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.email = email;
    }
}
