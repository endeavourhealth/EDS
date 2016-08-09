package org.endeavourhealth.transform.common.exceptions;

public class UnexpectedOrganisationException extends TransformException {

    private String localOrgId = null;
    private String odsCode = null;

    public UnexpectedOrganisationException(String localOrgId, String odsCode) {
        this(localOrgId, odsCode, null);
    }
    public UnexpectedOrganisationException(String localOrgId, String odsCode, Throwable cause) {
        super("Unexpected content for organisation local ID: " + localOrgId + " ODS code: " + odsCode, cause);
        this.localOrgId = localOrgId;
        this.odsCode = odsCode;
    }

    public String getLocalOrgId() {
        return localOrgId;
    }

    public String getOdsCode() {
        return odsCode;
    }
}
