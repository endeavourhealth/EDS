package org.endeavourhealth.transform.hl7v2.parser.datatypes;

public interface ICx {
    String getId();
    String getCheckDigit();
    String getCheckDigitCodeScheme();
    String getAssigningAuthority();
    String getIdentifierTypeCode();
    String getAssigningFacility();
}
