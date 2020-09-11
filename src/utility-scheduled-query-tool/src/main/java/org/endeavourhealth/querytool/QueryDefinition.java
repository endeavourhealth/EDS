package org.endeavourhealth.querytool;

import java.util.List;

public class QueryDefinition {

    private String description;
    private String databaseConfigName;
    private String databaseInstanceName;
    private List<Query> queries;
    private String emailSubject;
    private String emailBody;
    private List<String> emailRecipients;

    public QueryDefinition() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDatabaseConfigName() {
        return databaseConfigName;
    }

    public void setDatabaseConfigName(String databaseConfigName) {
        this.databaseConfigName = databaseConfigName;
    }

    public String getDatabaseInstanceName() {
        return databaseInstanceName;
    }

    public void setDatabaseInstanceName(String databaseInstanceName) {
        this.databaseInstanceName = databaseInstanceName;
    }

    public List<Query> getQueries() {
        return queries;
    }

    public void setQueries(List<Query> queries) {
        this.queries = queries;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }

    public List<String> getEmailRecipients() {
        return emailRecipients;
    }

    public void setEmailRecipients(List<String> emailRecipients) {
        this.emailRecipients = emailRecipients;
    }
}
