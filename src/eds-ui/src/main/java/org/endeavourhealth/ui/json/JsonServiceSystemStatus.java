package org.endeavourhealth.ui.json;

import java.util.Date;

public class JsonServiceSystemStatus {
    private String systemName;
    private Date lastDataReceived;
    private boolean isProcessingUpToDate;
    private boolean isProcessingInError;
    private Date lastDataProcessedDate;

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public Date getLastDataReceived() {
        return lastDataReceived;
    }

    public void setLastDataReceived(Date lastDataReceived) {
        this.lastDataReceived = lastDataReceived;
    }

    public boolean isProcessingUpToDate() {
        return isProcessingUpToDate;
    }

    public void setProcessingUpToDate(boolean processingUpToDate) {
        isProcessingUpToDate = processingUpToDate;
    }

    public boolean isProcessingInError() {
        return isProcessingInError;
    }

    public void setProcessingInError(boolean processingInError) {
        isProcessingInError = processingInError;
    }

    public Date getLastDataProcessedDate() {
        return lastDataProcessedDate;
    }

    public void setLastDataProcessedDate(Date lastDataProcessedDate) {
        this.lastDataProcessedDate = lastDataProcessedDate;
    }
}
