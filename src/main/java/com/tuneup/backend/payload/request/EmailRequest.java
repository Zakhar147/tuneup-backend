package com.tuneup.backend.payload.request;

public class EmailRequest {
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "EmailRequest{" +
                "email='" + email + '\'' +
                '}';
    }
}
