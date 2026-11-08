package org.endeavourhealth.ui.json.security;

import java.util.UUID;

public class JsonKeycloakUser {

    private UUID userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    public JsonKeycloakUser() {
    }

    public JsonKeycloakUser(UUID userId, String username, String email, String firstName, String lastName) {

        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
