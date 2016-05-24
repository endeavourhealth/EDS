package org.endeavourhealth.transform.emis.openhr.transforms.common;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.DtCodeQualified;
import org.endeavourhealth.transform.fhir.FhirUris;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;

public class CodeConverter
{
    private static final String OPENHR_IDENTIFIER_READ2 = "2.16.840.1.113883.2.1.6.2";
    private static final String OPENHR_IDENTIFIER_SNOMED = "2.16.840.1.113883.2.1.3.2.4.15";
    private static final String OPENHR_IDENTIFIER_EMIS_SNOMED = "2.16.840.1.113883.2.1.6.3";
    private static final String OPENHR_IDENTIFIER_EMIS_PREPARATION = "EMISPREPARATION";

    public static CodeableConcept convertCode(DtCodeQualified sourceCode) throws TransformException
    {
        return convertCode(sourceCode, null);
    }

    public static CodeableConcept convertCode(DtCodeQualified sourceCode, String sourceTerm) throws TransformException
    {
        CodeableConcept result = new CodeableConcept();

        if (StringUtils.isNotBlank(sourceTerm))
            result.setText(sourceTerm);

        if (sourceCode != null)
            addCode(result, sourceCode);

        return result;
    }

    private static void addCode(CodeableConcept codeableConcept, DtCodeQualified sourceCode) throws TransformException
    {
        codeableConcept.addCoding(new Coding()
                .setCode(sourceCode.getCode())
                .setDisplay(sourceCode.getDisplayName())
                .setSystem(convertCodeSystem(sourceCode.getCodeSystem())));

        if (sourceCode.getTranslation() != null)
            for (DtCodeQualified code : sourceCode.getTranslation())
                addCode(codeableConcept, code);

    }

    private static String convertCodeSystem(String sourceCodeSystem) throws TransformException
    {
        switch (sourceCodeSystem)
        {
            case OPENHR_IDENTIFIER_READ2:
                return FhirUris.CODE_SYSTEM_READ2;
            case OPENHR_IDENTIFIER_SNOMED:
                return FhirUris.CODE_SYSTEM_SNOMED_CT;
            case OPENHR_IDENTIFIER_EMIS_SNOMED:
                return FhirUris.CODE_SYSTEM_EMISSNOMED;
            case OPENHR_IDENTIFIER_EMIS_PREPARATION:
                return FhirUris.CODE_SYSTEM_EMISPREPARATION;
            default:
                throw new TransformException("CodeSystem not supported: " + sourceCodeSystem);
        }
    }

    public static boolean hasQualifier(DtCodeQualified code, String name, String value)
    {
        if (code == null)
            return false;

        return code.getQualifier()
                .stream()
                .anyMatch(q -> q.getName().getDisplayName().equals(name)
                        && q.getValue().getDisplayName().equals(value));
    }

    public static boolean isBloodPressureCode(DtCodeQualified code)
    {
        if (code == null)
            return false;

        if (code.getCodeSystem().equals(OPENHR_IDENTIFIER_READ2) && code.getCode().equals("246"))
            return true;

        if (code.getTranslation() != null)
        {
            if (code.getTranslation()
                    .stream()
                    .anyMatch(t -> t.getCodeSystem().equals(OPENHR_IDENTIFIER_READ2)
                            && t.getCode().equals("246"))) {
                return true;
            }
        }

        return false;
    }
}
