package com.tuneup.backend.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse {
    private String accestoken;
    private String username;
    private String email;

    public JwtResponse(String token, String username, String email) {
        this.accestoken = token;
        this.username = username;
        this.email = email;
    }
}
