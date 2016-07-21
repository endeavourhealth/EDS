package org.endeavourhealth.core.data.admin.models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.UUID;

@Table(keyspace = "admin", name = "end_user")
public class EndUser {
    @PartitionKey
    @Column(name = "id")
    private UUID id;
    @Column(name = "title")
    private String title;
    @Column(name = "forename")
    private String forename;
    @Column(name = "surname")
    private String surname;
    @Column(name = "email")
    private String email;
    @Column(name = "is_super_user")
    private Boolean isSuperUser;

    public EndUser() {
    }

    public EndUser(UUID id, String title, String forename, String surname, String email, Boolean isSuperUser) {
        this.id = id;
        this.title = title;
        this.forename = forename;
        this.surname = surname;
        this.email = email;
        this.isSuperUser = isSuperUser;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getIsSuperUser() {
        return isSuperUser;
    }

    public void setIsSuperUser(Boolean isSuperUser) {
        this.isSuperUser = isSuperUser;
    }

    @Override
    public String toString() {
        return "EndUser{" +
                "id=" + id +
                ", forename='" + forename + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}