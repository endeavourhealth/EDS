package org.endeavourhealth.transform.common.exceptions;

public class FileFormatException extends TransformException {

    private String fileName = null;

    public FileFormatException(String fileName, String message) {
        this(fileName, message, null);
    }
    public FileFormatException(String fileName, String message, Throwable cause) {
        super(message, cause);
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
