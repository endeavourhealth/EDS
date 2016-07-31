package org.endeavourhealth.transform.common.exceptions;

public class VersionNotSupportedException extends TransformException {

    private String software = null;
    private String version = null;

    public VersionNotSupportedException(String software, String version) {
        this(software, version, null);
    }
    public VersionNotSupportedException(String software, String version, Throwable cause) {
        super("Unsupported version for " + software + " transformer: " + version, cause);
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
