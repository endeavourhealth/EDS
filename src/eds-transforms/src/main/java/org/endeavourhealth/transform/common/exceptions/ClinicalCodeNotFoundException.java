package org.endeavourhealth.transform.common.exceptions;

public class ClinicalCodeNotFoundException extends TransformException {

    private Long codeId = null;
    private boolean medication = false;
    private String dataSharingAgreementId;

    public ClinicalCodeNotFoundException(String dataSharingAgreementId, boolean medication, Long codeId) {
        this(dataSharingAgreementId, medication, codeId, null);
    }
    public ClinicalCodeNotFoundException(String dataSharingAgreementId, boolean medication, Long codeId, Throwable cause) {
        super("Failed to find " + (medication ? "medication" : "clinical code") + " CodeableConcept for codeId " + codeId + " and sharing agreement " + dataSharingAgreementId, cause);
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
