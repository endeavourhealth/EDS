package org.endeavourhealth.transform.fhir.schema;

import org.endeavourhealth.transform.fhir.FhirValueSetUri;

public enum ProblemRelationshipType {

    COMBINED("combined", "Combined"), //This problem is combined/merged with the target problem to form a single logical problem
    GROUPED("grouped", "Grouped"), //This problem is part of a group that includes the target problem as a member of the group
    REPLACES("replaces", "Replaces"), //This problem replaces a previous problem (i.e. a revised diagnosis). The target problem is now obsolete
    EVOLVED_FROM("evolved-from", "Evolved From"); //This problem has evolved from the target problem

    private String code = null;
    private String description = null;

    public String getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
    public String getSystem() {
        return FhirValueSetUri.VALUE_SET_PROBLEM_RELATIONSHIP_TYPE;
    }


    ProblemRelationshipType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
