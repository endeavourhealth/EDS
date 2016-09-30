package org.endeavourhealth.transform.ui.models;

import java.util.List;

public class UIHumanName {
    private String familyName;
    private String[] givenNames;
    private String prefix;

    public String getFamilyName() {
        return familyName;
    }

    public UIHumanName setFamilyName(String familyName) {
        this.familyName = familyName;
        return this;
    }

    public String[] getGivenNames() {
        return givenNames;
    }

    public UIHumanName setGivenNames(List<String> givenNames) {
        this.givenNames = givenNames.toArray(new String[givenNames.size()]);
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public UIHumanName setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }
}
