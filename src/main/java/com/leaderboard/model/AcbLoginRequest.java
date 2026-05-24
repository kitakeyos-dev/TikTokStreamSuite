package com.leaderboard.model;

/**
 * Request payload for ACB login endpoint.
 */
public class AcbLoginRequest {
    private String username;
    private String password;
    private String clientId;

    public AcbLoginRequest() {
    }

    public AcbLoginRequest(String username, String password, String clientId) {
        this.username = username;
        this.password = password;
        this.clientId = clientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
