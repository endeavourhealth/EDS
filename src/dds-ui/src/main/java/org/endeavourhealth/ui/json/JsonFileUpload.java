package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonFileUpload {
    private String serverNumber = null;
    private String serverHostName = null;
    private String name = null;
    private String fileData = null;
    private Byte success = null;
    private String file = null;

    public String getServerNumber() {
        return serverNumber;
    }

    public void setServerNumber(String serverNumber) {
        this.serverNumber = serverNumber;
    }

    public String getServerHostName() {
        return serverHostName;
    }

    public void setServerHostName(String serverHostName) {
        this.serverHostName = serverHostName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileData() {
        return fileData;
    }

    public void setFileData(String fileData) {
        this.fileData = fileData;
    }

    public Byte getSuccess() {
        return success;
    }

    public void setSuccess(Byte success) {
        this.success = success;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
