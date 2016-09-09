package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.core.data.admin.models.EndUser;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonEndUser {

    private UUID uuid = null;
    private String username = null;
    private String password = null;
    private String title = null;
    private String forename = null;
    private String surname = null;
    private Boolean superUser = null; //using non-primative types because serialisation to JSON can skip nulls, if we want
    private Boolean admin = null;
    private Integer permissions = null; //to be removed after isAdmin is adopted
    private Boolean mustChangePassword = null;

    public JsonEndUser() {
    }

    public JsonEndUser(UserRepresentation keycloakUser) {
        this.uuid = UUID.fromString(keycloakUser.getId());
        this.username = keycloakUser.getUsername();
        this.forename = keycloakUser.getFirstName();
        this.surname = keycloakUser.getLastName();
    }

    public JsonEndUser(EndUser endUser, Boolean isAdmin, Boolean mustChangePassword) {
        this.uuid = endUser.getId();
        this.username = endUser.getEmail();
        this.title = endUser.getTitle();
        this.forename = endUser.getForename();
        this.surname = endUser.getSurname();
        this.superUser = new Boolean(endUser.getIsSuperUser());
        this.admin = isAdmin;
        this.mustChangePassword = mustChangePassword;

        //to be removed once web client changed to use isAdmin
        if (isAdmin != null) {
            if (isAdmin) {
                this.permissions = new Integer(2);
            } else {
                this.permissions = new Integer(1);
            }
        }
    }

    /**
     * gets/sets
     */
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public Boolean getSuperUser() {
        return superUser;
    }

    public void setSuperUser(Boolean superUser) {
        this.superUser = superUser;
    }

    public Integer getPermissions() {
        return permissions;
    }

    public void setPermissions(Integer permissions) {
        this.permissions = permissions;
    }

    public Boolean getMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(Boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
}
