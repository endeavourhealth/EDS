package org.endeavourhealth.ui.json;

import java.util.Date;
import java.util.UUID;

public class JsonSubscriberPublisherSystem {

    private UUID uuid; //system UUID
    private String name; //Emis, TPP, etc
    private String publisherMode; //regular, bulk, auto-fail, draft

    //data receipt into DDS
    private Date lastReceivedTimestamp;
    private Date lastReceivedExtractDate;
    private Date lastReceivedExtractCutoff;

    //INBOUND data processing
    private boolean processingInError;
    private String processingInErrorMessage;
    private Date lastProcessedInTimestamp;
    private Date lastProcessedInExtractDate;
    private Date lastProcessedInExtractCutoff;

    //OUTBOUND data processing
    private Date lastProcessedOutTimestamp;
    private Date lastProcessedOutExtractDate;
    private Date lastProcessedOutExtractCutoff;

    //OUTBOUND data writing
    private Date lastWriteOutTimestamp;
    private Date lastWriteOutExtractDate;
    private Date lastWriteOutExtractCutoff;
    private boolean writeOutError;
    private String writeOutErrorMessage;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublisherMode() {
        return publisherMode;
    }

    public void setPublisherMode(String publisherMode) {
        this.publisherMode = publisherMode;
    }

    public Date getLastReceivedTimestamp() {
        return lastReceivedTimestamp;
    }

    public void setLastReceivedTimestamp(Date lastReceivedTimestamp) {
        this.lastReceivedTimestamp = lastReceivedTimestamp;
    }

    public Date getLastReceivedExtractDate() {
        return lastReceivedExtractDate;
    }

    public void setLastReceivedExtractDate(Date lastReceivedExtractDate) {
        this.lastReceivedExtractDate = lastReceivedExtractDate;
    }

    public Date getLastReceivedExtractCutoff() {
        return lastReceivedExtractCutoff;
    }

    public void setLastReceivedExtractCutoff(Date lastReceivedExtractCutoff) {
        this.lastReceivedExtractCutoff = lastReceivedExtractCutoff;
    }

    public boolean isProcessingInError() {
        return processingInError;
    }

    public void setProcessingInError(boolean processingInError) {
        this.processingInError = processingInError;
    }

    public String getProcessingInErrorMessage() {
        return processingInErrorMessage;
    }

    public void setProcessingInErrorMessage(String processingInErrorMessage) {
        this.processingInErrorMessage = processingInErrorMessage;
    }

    public Date getLastProcessedInTimestamp() {
        return lastProcessedInTimestamp;
    }

    public void setLastProcessedInTimestamp(Date lastProcessedInTimestamp) {
        this.lastProcessedInTimestamp = lastProcessedInTimestamp;
    }

    public Date getLastProcessedInExtractDate() {
        return lastProcessedInExtractDate;
    }

    public void setLastProcessedInExtractDate(Date lastProcessedInExtractDate) {
        this.lastProcessedInExtractDate = lastProcessedInExtractDate;
    }

    public Date getLastProcessedInExtractCutoff() {
        return lastProcessedInExtractCutoff;
    }

    public void setLastProcessedInExtractCutoff(Date lastProcessedInExtractCutoff) {
        this.lastProcessedInExtractCutoff = lastProcessedInExtractCutoff;
    }

    public Date getLastProcessedOutTimestamp() {
        return lastProcessedOutTimestamp;
    }

    public void setLastProcessedOutTimestamp(Date lastProcessedOutTimestamp) {
        this.lastProcessedOutTimestamp = lastProcessedOutTimestamp;
    }

    public Date getLastProcessedOutExtractDate() {
        return lastProcessedOutExtractDate;
    }

    public void setLastProcessedOutExtractDate(Date lastProcessedOutExtractDate) {
        this.lastProcessedOutExtractDate = lastProcessedOutExtractDate;
    }

    public Date getLastProcessedOutExtractCutoff() {
        return lastProcessedOutExtractCutoff;
    }

    public void setLastProcessedOutExtractCutoff(Date lastProcessedOutExtractCutoff) {
        this.lastProcessedOutExtractCutoff = lastProcessedOutExtractCutoff;
    }

    public Date getLastWriteOutTimestamp() {
        return lastWriteOutTimestamp;
    }

    public void setLastWriteOutTimestamp(Date lastWriteOutTimestamp) {
        this.lastWriteOutTimestamp = lastWriteOutTimestamp;
    }

    public Date getLastWriteOutExtractDate() {
        return lastWriteOutExtractDate;
    }

    public void setLastWriteOutExtractDate(Date lastWriteOutExtractDate) {
        this.lastWriteOutExtractDate = lastWriteOutExtractDate;
    }

    public Date getLastWriteOutExtractCutoff() {
        return lastWriteOutExtractCutoff;
    }

    public void setLastWriteOutExtractCutoff(Date lastWriteOutExtractCutoff) {
        this.lastWriteOutExtractCutoff = lastWriteOutExtractCutoff;
    }

    public boolean isWriteOutError() {
        return writeOutError;
    }

    public void setWriteOutError(boolean writeOutError) {
        this.writeOutError = writeOutError;
    }

    public String getWriteOutErrorMessage() {
        return writeOutErrorMessage;
    }

    public void setWriteOutErrorMessage(String writeOutErrorMessage) {
        this.writeOutErrorMessage = writeOutErrorMessage;
    }
}
