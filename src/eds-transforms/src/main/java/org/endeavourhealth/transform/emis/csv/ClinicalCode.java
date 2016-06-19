package org.endeavourhealth.transform.emis.csv;

import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.transform.fhir.CodingHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.terminology.Snomed;
import org.endeavourhealth.transform.terminology.TerminologyService;
import org.hl7.fhir.instance.model.CodeableConcept;

/**
 * temporary storage object used during parsing the EMIS CSV extract
 */
public class ClinicalCode {

    private String emisTerm = null;
    private String emisCode = null;
    private String emisCategory = null;

    private String nationalCodeCategory = null;
    private String nationalCode = null;
    private String nationalDescription = null;

    private Long snomedConceptId = null;
    private Long snomedDescriptionId = null;

    public ClinicalCode(String emisTerm, String emisCode, String emisCategory,
                        String nationalCodeCategory, String nationalCode, String nationalDescription,
                        Long snomedConceptId, Long snomedDescriptionId) {

        this.emisTerm = emisTerm;
        this.emisCode = emisCode;
        this.emisCategory = emisCategory;
        this.nationalCodeCategory = nationalCodeCategory;
        this.nationalCode = nationalCode;
        this.nationalDescription = nationalDescription;
        this.snomedConceptId = snomedConceptId;
        this.snomedDescriptionId = snomedDescriptionId;
    }

    public String getEmisTerm() {
        return emisTerm;
    }

    public String getEmisCode() {
        return emisCode;
    }

    public String getEmisCategory() {
        return emisCategory;
    }

    public String getNationalCodeCategory() {
        return nationalCodeCategory;
    }

    public String getNationalCode() {
        return nationalCode;
    }

    public String getNationalDescription() {
        return nationalDescription;
    }

    public Long getSnomedConceptId() {
        return snomedConceptId;
    }

    public Long getSnomedDescriptionId() {
        return snomedDescriptionId;
    }

    public CodeableConcept createCodeableConcept() {

        if (emisCode == null) {
            return CodeableConceptHelper.createCodeableConcept(emisTerm);
        }

        CodeableConcept ret = CodeableConceptHelper.createCodeableConcept(FhirUri.CODE_SYSTEM_READ2, emisTerm, emisCode);
        String snomedTerm = Snomed.getTerm(snomedConceptId.longValue(), snomedDescriptionId.longValue());
        ret.addCoding(CodingHelper.createCoding(FhirUri.CODE_SYSTEM_SNOMED_CT, snomedTerm, snomedConceptId.toString()));
        return ret;
    }
}