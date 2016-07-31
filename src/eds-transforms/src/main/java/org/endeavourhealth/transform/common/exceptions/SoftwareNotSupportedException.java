package org.endeavourhealth.transform.common.exceptions;

public class SoftwareNotSupportedException extends TransformException {

    private String software = null;
    private String version = null;

    public SoftwareNotSupportedException(String software, String version) {
        this(software, version, null);
    }
    public SoftwareNotSupportedException(String software, String version, Throwable cause) {
        super("Unsupported software: " + software + " with version: " + version, cause);
        this.software = software;
        this.version = version;
    }

    public String getSoftware() {
        return software;
    }

    public String getVersion() {
        return version;
    }

}
