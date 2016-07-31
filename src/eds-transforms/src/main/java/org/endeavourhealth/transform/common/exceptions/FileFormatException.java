package org.endeavourhealth.transform.common.exceptions;

import java.io.File;

public class FileFormatException extends TransformException {

    private String fileName = null;

    public FileFormatException(File file, String message) {
        this(file.getAbsolutePath(), message, null);
    }
    public FileFormatException(File file, String message, Throwable cause) {
        this(file.getAbsolutePath(), message, cause);
    }

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
