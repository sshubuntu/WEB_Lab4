package com.sshubuntu.weblab4.dto;

import com.sshubuntu.weblab4.entity.UserAccount;

public class AuthResponse {
    private String username;

    public static AuthResponse from(UserAccount user) {
        AuthResponse dto = new AuthResponse();
        dto.setUsername(user.getUsername());
        return dto;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}




