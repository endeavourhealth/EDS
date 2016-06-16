package org.endeavourhealth.transform.emis.emisopen.transforms.common;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.IntegerCodeType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.StringCodeType;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;

public class CodeConverter
{
    private static final String EMISOPEN_IDENTIFIER_EMIS_PREPARATION = "EMISPREPARATION";
    private static final String EMISOPEN_IDENTIFIER_SNOMED = "SNOMED";
    private static final String EMISOPEN_IDENTIFIER_READ2 = "READ2";

    public static CodeableConcept convert(StringCodeType code, String descriptiveText) throws TransformException
    {
        CodeableConcept codeableConcept = new CodeableConcept();

        Coding coding = new Coding()
                .setSystem(getCodingSystem(code.getScheme()))
                .setCode(code.getValue())
                .setDisplay(code.getTerm())
                .setUserSelected(true);

        codeableConcept.addCoding(coding);

        if (StringUtils.isNotBlank(code.getMapCode()))
        {
            Coding mapCoding = new Coding()
                    .setCode(code.getMapCode())
                    .setSystem(getCodingSystem(code.getMapScheme()));

            codeableConcept.addCoding(mapCoding);
        }

        if (StringUtils.isNotBlank(descriptiveText))
            codeableConcept.setText(descriptiveText);

        return codeableConcept;
    }

    public static CodeableConcept convert(IntegerCodeType drug) throws TransformException
    {
        CodeableConcept codeableConcept = new CodeableConcept();

        Coding coding = new Coding()
                .setSystem(getCodingSystem(drug.getScheme()))
                .setDisplay(drug.getTerm())
                .setUserSelected(true);

        if (!drug.getValue().equals(-1))
            coding.setCode(drug.getValue().toString());
        else
            coding.setCode(drug.getOldCode());

        codeableConcept.addCoding(coding);

        if (StringUtils.isNotBlank(drug.getMapCode()))
        {
            Coding mapCoding = new Coding()
                    .setCode(drug.getMapCode())
                    .setSystem(getCodingSystem(drug.getMapScheme()));

            codeableConcept.addCoding(mapCoding);
        }

        return codeableConcept;
    }

    private static String getCodingSystem(String scheme) throws TransformException
    {
        switch (scheme)
        {
            case EMISOPEN_IDENTIFIER_READ2: return FhirUri.CODE_SYSTEM_READ2;
            case EMISOPEN_IDENTIFIER_EMIS_PREPARATION: return FhirUri.CODE_SYSTEM_EMISPREPARATION;
            case EMISOPEN_IDENTIFIER_SNOMED: return FhirUri.CODE_SYSTEM_SNOMED_CT;
            default: throw new TransformException("Coding scheme not recognised");
        }
    }
}
