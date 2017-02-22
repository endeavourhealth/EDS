package org.endeavourhealth.transform.emis.emisopen.transforms.common;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.core.data.admin.CodeRepository;
import org.endeavourhealth.core.data.admin.models.SnomedLookup;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.IntegerCodeType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.StringCodeType;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.transform.fhir.CodingHelper;
import org.endeavourhealth.common.fhir.FhirUri;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;

public class CodeConverter
{
    private static final String EMISOPEN_IDENTIFIER_EMIS_PREPARATION = "EMISPREPARATION";
    private static final String EMISOPEN_IDENTIFIER_SNOMED = "SNOMED";
    private static final String EMISOPEN_IDENTIFIER_READ2 = "READ2";

    private static CodeRepository repository = new CodeRepository();

    public static CodeableConcept convert(StringCodeType code, String descriptiveText) throws TransformException {

        //we've had some events with a null code, so just return a textual codeable concept
        if (code == null) {
            return CodeableConceptHelper.createCodeableConcept(descriptiveText);
        }

        String emisCode = code.getValue();
        String emisTerm = code.getTerm();

        //the CSV uses a hyphen to delimit the synonym ID from the code, but since we include
        //the original term text anyway, there's no need to carry the synonym ID into the FHIR data
        String emisCodeNoSynonym = emisCode;
        int index = emisCodeNoSynonym.indexOf("-");
        if (index > -1) {
            emisCodeNoSynonym = emisCodeNoSynonym.substring(0, index);
        }

        CodeableConcept fhirConcept = null;

        //without a Read 2 engine, there seems to be no cast-iron way to determine whether the supplied codes
        //are Read 2 codes or Emis local codes. Looking at the codes from the test data sets, this seems
        //to be a reliable way to perform the same check.
        if (emisCode.startsWith("EMIS")
                || emisCode.startsWith("ALLERGY")
                || emisCode.startsWith("EGTON")
                || emisCodeNoSynonym.length() > 5) {

            fhirConcept = CodeableConceptHelper.createCodeableConcept(FhirUri.CODE_SYSTEM_EMIS_CODE, emisTerm, emisCode);

        } else {

            //Emis store Read 2 codes without the padding stops, which seems to be against Read 2 standards,
            //so make sure all codes are padded to five chars
            while (emisCode.length() < 5) {
                emisCode += ".";
            }

            //should ideally be able to distringuish between Read2 and EMIS codes
            fhirConcept = CodeableConceptHelper.createCodeableConcept(FhirUri.CODE_SYSTEM_READ2, emisTerm, emisCode);
        }

        String mappedCode = code.getMapCode();
        if (StringUtils.isNotBlank(mappedCode)) {

            String mappedTerm = emisTerm;
            String system = getCodingSystem(code.getMapScheme());

            //if the system is proper SNOMED, then get the official term for the snomed concept ID
            if (system == FhirUri.CODE_SYSTEM_SNOMED_CT) {

                SnomedLookup snomedLookup = repository.getSnomedLookup(mappedCode);
                if (snomedLookup != null) {
                    mappedTerm = snomedLookup.getTerm();
                }
            }

            fhirConcept.addCoding(CodingHelper.createCoding(system, mappedTerm, mappedCode));
        }

        if (!Strings.isNullOrEmpty(descriptiveText)) {
            fhirConcept.setText(descriptiveText);
        }

        return fhirConcept;
    }
    /*public static CodeableConcept convert(StringCodeType code, String descriptiveText) throws TransformException
    {
        CodeableConcept codeableConcept = new CodeableConcept();

        Coding coding = new Coding()
                .setSystem(getCodingSystem(code.getScheme()))
                .setCode(code.getValue())
                .setDisplay(code.getTerm())
                .setUserSelected(true);

        if (coding.getSystem().equals(FhirUri.CODE_SYSTEM_READ2))
            if (coding.getCode() != null)
                coding.setCode(coding.getCode().replace(".", ""));

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
    }*/

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
        switch (scheme) {
            case EMISOPEN_IDENTIFIER_READ2:
                return FhirUri.CODE_SYSTEM_READ2;
            case EMISOPEN_IDENTIFIER_EMIS_PREPARATION:
                return FhirUri.CODE_SYSTEM_EMISPREPARATION;
            case EMISOPEN_IDENTIFIER_SNOMED:
                return FhirUri.CODE_SYSTEM_SNOMED_CT;
            default:
                //log the scheme out
                throw new TransformException("Coding scheme not recognised " + scheme);
                //throw new TransformException("Coding scheme not recognised");
        }
    }
}
