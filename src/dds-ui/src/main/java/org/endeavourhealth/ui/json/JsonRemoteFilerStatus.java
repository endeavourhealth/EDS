package org.endeavourhealth.ui.json;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JsonRemoteFilerStatus {

    private int subscriberId;
    private int countQueuedMessages;
    private Date oldestQueuedMessage;
    private Date newestQueuedMessage;
    private int countMessagesWrittenToSftp;
    private Date oldestMessageWrittenToSftp;
    private Date newestMessageWrittenToSftp;
    private int countMessagesInError;
    private Date oldestMessageInError;
    private Date newestMessageInError;
    private List<JsonRemoteFilerError> errors = new ArrayList<>();

    public int getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(int subscriberId) {
        this.subscriberId = subscriberId;
    }

    public int getCountQueuedMessages() {
        return countQueuedMessages;
    }

    public void setCountQueuedMessages(int countQueuedMessages) {
        this.countQueuedMessages = countQueuedMessages;
    }

    public Date getOldestQueuedMessage() {
        return oldestQueuedMessage;
    }

    public void setOldestQueuedMessage(Date oldestQueuedMessage) {
        this.oldestQueuedMessage = oldestQueuedMessage;
    }

    public Date getNewestQueuedMessage() {
        return newestQueuedMessage;
    }

    public void setNewestQueuedMessage(Date newestQueuedMessage) {
        this.newestQueuedMessage = newestQueuedMessage;
    }

    public int getCountMessagesWrittenToSftp() {
        return countMessagesWrittenToSftp;
    }

    public void setCountMessagesWrittenToSftp(int countMessagesWrittenToSftp) {
        this.countMessagesWrittenToSftp = countMessagesWrittenToSftp;
    }

    public Date getOldestMessageWrittenToSftp() {
        return oldestMessageWrittenToSftp;
    }

    public void setOldestMessageWrittenToSftp(Date oldestMessageWrittenToSftp) {
        this.oldestMessageWrittenToSftp = oldestMessageWrittenToSftp;
    }

    public Date getNewestMessageWrittenToSftp() {
        return newestMessageWrittenToSftp;
    }

    public void setNewestMessageWrittenToSftp(Date newestMessageWrittenToSftp) {
        this.newestMessageWrittenToSftp = newestMessageWrittenToSftp;
    }

    public int getCountMessagesInError() {
        return countMessagesInError;
    }

    public void setCountMessagesInError(int countMessagesInError) {
        this.countMessagesInError = countMessagesInError;
    }

    public Date getOldestMessageInError() {
        return oldestMessageInError;
    }

    public void setOldestMessageInError(Date oldestMessageInError) {
        this.oldestMessageInError = oldestMessageInError;
    }

    public Date getNewestMessageInError() {
        return newestMessageInError;
    }

    public void setNewestMessageInError(Date newestMessageInError) {
        this.newestMessageInError = newestMessageInError;
    }

    public List<JsonRemoteFilerError> getErrors() {
        return errors;
    }

    public void setErrors(List<JsonRemoteFilerError> errors) {
        this.errors = errors;
    }
}
