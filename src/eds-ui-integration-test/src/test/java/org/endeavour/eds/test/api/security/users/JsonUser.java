package org.endeavour.eds.test.api.security.users;

import java.util.UUID;

public class JsonUser {

    private UUID userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    public JsonUser() {
    }

    public JsonUser(UUID userId, String username, String email, String firstName, String lastName) {
        this.username = username;
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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
