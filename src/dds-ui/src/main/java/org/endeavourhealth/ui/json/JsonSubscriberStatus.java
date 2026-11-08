package org.endeavourhealth.ui.json;

import java.util.ArrayList;
import java.util.List;

public class JsonSubscriberStatus {
    private String name;
    private String description;
    private String schema;
    private boolean deidentified;
    private boolean excludeTestPatients;
    private String excludeNhsNumberRegex;
    private boolean excludePatientsWithoutNhsNumber;
    private Integer remoteSubscriberId;
    private String subscriberLocation;
    private String cohortType;
    private List<String> cohort;
    private String cohortDesc; //textual version of the above two
    private String subscriberDatabase;
    private String subscriberDatabaseName;
    private String subscriberTransformDatabase;
    private String subscriberTransformDatabaseName;
    private int numPublishers = 0; //if a subscriber has no publishers, then the below shouldn't be left un-initialised, so set all to zero
    private int receivedOneDay = 0;
    private int receivedTwoDays = 0;
    private int receivedMoreDays = 0;
    private int inboundError = 0;
    private int inboundUpToDate = 0;
    private int inboundOneDay = 0;
    private int inboundMoreDays = 0;
    private int outboundUpToDate = 0;
    private int outboundOneDay = 0;
    private int outboundMoreDays = 0;
    private int writeOutUpToDate = 0;
    private int writeOutOneDay = 0;
    private int writeOutMoreDays = 0;
    private int outboundError = 0;
    private List<JsonSubscriberPublisherService> publisherServices = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public boolean isDeidentified() {
        return deidentified;
    }

    public void setDeidentified(boolean deidentified) {
        this.deidentified = deidentified;
    }

    public boolean isExcludeTestPatients() {
        return excludeTestPatients;
    }

    public void setExcludeTestPatients(boolean excludeTestPatients) {
        this.excludeTestPatients = excludeTestPatients;
    }

    public String getExcludeNhsNumberRegex() {
        return excludeNhsNumberRegex;
    }

    public void setExcludeNhsNumberRegex(String excludeNhsNumberRegex) {
        this.excludeNhsNumberRegex = excludeNhsNumberRegex;
    }

    public boolean isExcludePatientsWithoutNhsNumber() {
        return excludePatientsWithoutNhsNumber;
    }

    public void setExcludePatientsWithoutNhsNumber(boolean excludePatientsWithoutNhsNumber) {
        this.excludePatientsWithoutNhsNumber = excludePatientsWithoutNhsNumber;
    }

    public Integer getRemoteSubscriberId() {
        return remoteSubscriberId;
    }

    public void setRemoteSubscriberId(Integer remoteSubscriberId) {
        this.remoteSubscriberId = remoteSubscriberId;
    }

    public String getSubscriberLocation() {
        return subscriberLocation;
    }

    public void setSubscriberLocation(String subscriberLocation) {
        this.subscriberLocation = subscriberLocation;
    }

    public String getCohortType() {
        return cohortType;
    }

    public void setCohortType(String cohortType) {
        this.cohortType = cohortType;
    }

    public List<String> getCohort() {
        return cohort;
    }

    public void setCohort(List<String> cohort) {
        this.cohort = cohort;
    }

    public String getCohortDesc() {
        return cohortDesc;
    }

    public void setCohortDesc(String cohortDesc) {
        this.cohortDesc = cohortDesc;
    }

    public String getSubscriberDatabase() {
        return subscriberDatabase;
    }

    public void setSubscriberDatabase(String subscriberDatabase) {
        this.subscriberDatabase = subscriberDatabase;
    }

    public String getSubscriberDatabaseName() {
        return subscriberDatabaseName;
    }

    public void setSubscriberDatabaseName(String subscriberDatabaseName) {
        this.subscriberDatabaseName = subscriberDatabaseName;
    }

    public String getSubscriberTransformDatabase() {
        return subscriberTransformDatabase;
    }

    public void setSubscriberTransformDatabase(String subscriberTransformDatabase) {
        this.subscriberTransformDatabase = subscriberTransformDatabase;
    }

    public String getSubscriberTransformDatabaseName() {
        return subscriberTransformDatabaseName;
    }

    public void setSubscriberTransformDatabaseName(String subscriberTransformDatabaseName) {
        this.subscriberTransformDatabaseName = subscriberTransformDatabaseName;
    }

    public int getNumPublishers() {
        return numPublishers;
    }

    public void setNumPublishers(int numPublishers) {
        this.numPublishers = numPublishers;
    }

    public int getReceivedOneDay() {
        return receivedOneDay;
    }

    public void setReceivedOneDay(int receivedOneDay) {
        this.receivedOneDay = receivedOneDay;
    }

    public int getReceivedTwoDays() {
        return receivedTwoDays;
    }

    public void setReceivedTwoDays(int receivedTwoDays) {
        this.receivedTwoDays = receivedTwoDays;
    }

    public int getReceivedMoreDays() {
        return receivedMoreDays;
    }

    public void setReceivedMoreDays(int receivedMoreDays) {
        this.receivedMoreDays = receivedMoreDays;
    }

    public int getInboundError() {
        return inboundError;
    }

    public void setInboundError(int inboundError) {
        this.inboundError = inboundError;
    }

    public int getInboundUpToDate() {
        return inboundUpToDate;
    }

    public void setInboundUpToDate(int inboundUpToDate) {
        this.inboundUpToDate = inboundUpToDate;
    }

    public int getInboundOneDay() {
        return inboundOneDay;
    }

    public void setInboundOneDay(int inboundOneDay) {
        this.inboundOneDay = inboundOneDay;
    }

    public int getInboundMoreDays() {
        return inboundMoreDays;
    }

    public void setInboundMoreDays(int inboundMoreDays) {
        this.inboundMoreDays = inboundMoreDays;
    }

    public int getOutboundUpToDate() {
        return outboundUpToDate;
    }

    public void setOutboundUpToDate(int outboundUpToDate) {
        this.outboundUpToDate = outboundUpToDate;
    }

    public int getOutboundOneDay() {
        return outboundOneDay;
    }

    public void setOutboundOneDay(int outboundOneDay) {
        this.outboundOneDay = outboundOneDay;
    }

    public int getOutboundMoreDays() {
        return outboundMoreDays;
    }

    public void setOutboundMoreDays(int outboundMoreDays) {
        this.outboundMoreDays = outboundMoreDays;
    }

    public int getWriteOutUpToDate() {
        return writeOutUpToDate;
    }

    public void setWriteOutUpToDate(int writeOutUpToDate) {
        this.writeOutUpToDate = writeOutUpToDate;
    }

    public int getWriteOutOneDay() {
        return writeOutOneDay;
    }

    public void setWriteOutOneDay(int writeOutOneDay) {
        this.writeOutOneDay = writeOutOneDay;
    }

    public int getWriteOutMoreDays() {
        return writeOutMoreDays;
    }

    public void setWriteOutMoreDays(int writeOutMoreDays) {
        this.writeOutMoreDays = writeOutMoreDays;
    }

    public int getOutboundError() {
        return outboundError;
    }

    public void setOutboundError(int outboundError) {
        this.outboundError = outboundError;
    }

    public List<JsonSubscriberPublisherService> getPublisherServices() {
        return publisherServices;
    }

    public void setPublisherServices(List<JsonSubscriberPublisherService> publisherServices) {
        this.publisherServices = publisherServices;
    }

    public void incrementNumPublishers() {
        this.numPublishers++;
    }

    public void incrementReceivedOneDay() {
        this.receivedOneDay++;
    }

    public void incrementReceivedTwoDays() {
        this.receivedTwoDays++;
    }

    public void incrementReceivedMoreDays() {
        this.receivedMoreDays++;
    }

    public void incrementInboundError() {
        this.inboundError++;
    }

    public void incrementInboundUpToDate() {
        this.inboundUpToDate++;
    }

    public void incrementInboundOneDay() {
        this.inboundOneDay++;
    }

    public void incrementInboundMoreDays() {
        this.inboundMoreDays++;
    }

    public void incrementOutboundUpToDate() {
        this.outboundUpToDate++;

    }

    public void incrementOutboundOneDay() {
        this.outboundOneDay++;
    }

    public void incrementOutboundMoreDays() {
        this.outboundMoreDays++;
    }

    public void incrementWriteOutUpToDate() {
        this.writeOutUpToDate++;
    }

    public void incrementWriteOutOneDay() {
        this.writeOutOneDay++;
    }

    public void incrementWriteOutMoreDays() {
        this.writeOutMoreDays++;
    }

    public void incrementOutboundError() {
        this.outboundError++;
    }
}
