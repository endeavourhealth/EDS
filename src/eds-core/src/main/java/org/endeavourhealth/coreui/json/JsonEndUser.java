package org.endeavourhealth.coreui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.endeavourhealth.common.security.models.EndUser;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonEndUser {

    private UUID uuid = null;
    private String username = null;
    private String password = null;
    private String title = null;
    private String forename = null;
    private String surname = null;
    private String email = null;
    private String mobile = null;
    private String photo = null;
    private String totp = null;
    private String defaultOrgId = null;   //v1 organisation-id attribute for patient-explorer reports

    private Boolean superUser = null; //using non-primative types because serialisation to JSON can skip nulls, if we want
    private Boolean admin = null;
    private Integer permissions = null; //to be removed after isEDSAdmin is adopted
    private Boolean mustChangePassword = null;

    private List<JsonEndUserRole> userRoles = null;


    public JsonEndUser() {
    }

    public JsonEndUser(UserRepresentation keycloakUser) {
        this.uuid = UUID.fromString(keycloakUser.getId());
        this.username = keycloakUser.getUsername();
        this.forename = keycloakUser.getFirstName();
        this.surname = keycloakUser.getLastName();
        this.email = keycloakUser.getEmail();
        this.totp = keycloakUser.isTotp()==true ? "yes" : "no";

        //Extract attributes such as mobile and photo, remove start and end [] chars
        Map<String, Object> userAttributes = keycloakUser.getAttributes();
        if (userAttributes != null) {
            for (String attributeKey : userAttributes.keySet()) {
                if (attributeKey.equalsIgnoreCase("mobile")) {
                    Object obj = userAttributes.get(attributeKey);
                    this.mobile = obj.toString().substring(1, obj.toString().length()-1);
                } else if (attributeKey.equalsIgnoreCase("photo")) {
                    Object obj = userAttributes.get(attributeKey);
                    this.photo = obj.toString().substring(1, obj.toString().length()-1);
                } else if (attributeKey.equalsIgnoreCase("organisation-id")) {
                    Object obj = userAttributes.get(attributeKey);
                    this.defaultOrgId = obj.toString().substring(1, obj.toString().length() - 1);
                }
            }
        }
    }


    public JsonEndUser(EndUser endUser, Boolean isAdmin, Boolean mustChangePassword) {
        this.uuid = endUser.getId();
        this.username = endUser.getEmail();
        this.title = endUser.getTitle();
        this.forename = endUser.getForename();
        this.surname = endUser.getSurname();
        this.email = endUser.getEmail();
        this.superUser = Boolean.valueOf(endUser.getIsSuperUser());
        this.admin = isAdmin;
        this.mustChangePassword = mustChangePassword;

        //to be removed once web client changed to use isEDSAdmin
        if (isAdmin != null) {
            if (isAdmin) {
                this.permissions = Integer.valueOf(2);
            } else {
                this.permissions = Integer.valueOf(1);
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

    public String getEmail() { return email; }

    public void setEmail() {this.email = email; }

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

    public List<JsonEndUserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<JsonEndUserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public String getMobile() {return mobile; }

    public void setMobile(String mobile) { this.mobile = mobile;}

    public String getPhoto() {return photo; }

    public void setPhoto(String photo) { this.photo = photo;}

    public String getDefaultOrgId() {return defaultOrgId; }

    public void setDefaultOrgId(String defaultOrgId) { this.defaultOrgId = defaultOrgId;}

    public String getTOTP() {return totp; }

    public void setTOTP(String totp) { this.totp = totp;}

}
