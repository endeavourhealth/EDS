package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * JSON object to allow sending back an exception message to clients after
 * a request has gone wrong on the server
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonServerException {
    private String message = null;

    public JsonServerException(String message) {
        this.message = message;
    }

    /**
     * gets/sets
     */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
