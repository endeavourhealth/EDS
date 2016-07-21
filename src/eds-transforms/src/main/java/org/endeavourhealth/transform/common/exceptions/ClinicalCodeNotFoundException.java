package org.endeavourhealth.transform.common.exceptions;

public class ClinicalCodeNotFoundException extends TransformException {

    private Long codeId = null;
    private boolean medication = false;

    public ClinicalCodeNotFoundException(Long codeId, boolean medication) {
        this(codeId, medication, null);
    }
    public ClinicalCodeNotFoundException(Long codeId, boolean medication, Throwable cause) {
        super("Failed to find " + (medication ? "medication" : "clinical code") + " CodeableConcept for codeId " + codeId, cause);
        this.codeId = codeId;
        this.medication = medication;
    }

    public Long getCodeId() {
        return codeId;
    }

    public boolean isMedication() {
        return medication;
    }
}
