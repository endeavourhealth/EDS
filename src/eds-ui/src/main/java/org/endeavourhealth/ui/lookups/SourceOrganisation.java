package org.endeavourhealth.ui.lookups;

public final class SourceOrganisation {
    private String odsCode = null;
    private String name = null;

    public SourceOrganisation(String odsCode, String name) {
        this.odsCode = odsCode;
        this.name = name;
    }

    /**
     * gets/sets
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOdsCode() {
        return odsCode;
    }

    public void setOdsCode(String odsCode) {
        this.odsCode = odsCode;
    }
}
