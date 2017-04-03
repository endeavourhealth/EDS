package org.endeavourhealth.transform.emis.emisopen.transforms.common;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.CodingHelper;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.core.data.admin.CodeRepository;
import org.endeavourhealth.core.data.admin.models.SnomedLookup;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.IntegerCodeType;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.StringCodeType;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;

public class CodeConverter
{
    private static final String EMISOPEN_IDENTIFIER_EMIS_PREPARATION = "EMISPREPARATION";
    private static final String EMISOPEN_IDENTIFIER_SNOMED = "SNOMED";
    private static final String EMISOPEN_IDENTIFIER_READ2 = "READ2";
    private static final String EMISOPEN_IDENTIFIER_EMIS_BOTH = "EMISBOTH";
    private static final String EMISOPEN_IDENTIFIER_EMIS_DRUG_NAME = "EMISDRUGNAME";
    private static final String EMISOPEN_IDENTIFIER_EMIS_DRUG_GROUP = "EMISDRUGGROUP";
    private static final String EMISOPEN_IDENTIFIER_EMIS_NON_DRUG_ALLERGY = "EMISNONDRUGALLERGY";
    private static final String EMISOPEN_IDENTIFIER_EMIS_CONSTITUENT = "EMISCONSTITUENT";

    private static CodeRepository repository = new CodeRepository();

    public static CodeableConcept convert(StringCodeType code, String descriptiveText) throws TransformException {

        //we've had some events with a null code, so just return a textual codeable concept
        if (code == null) {
            return CodeableConceptHelper.createCodeableConcept(descriptiveText);
        }

        String emisCode = code.getValue();
        String emisTerm = code.getTerm();
        String emisScheme = code.getScheme();

        String system = getCodingSystem(emisScheme);

        //the CSV uses a hyphen to delimit the synonym ID from the code, but since we include
        //the original term text anyway, there's no need to carry the synonym ID into the FHIR data
        String emisCodeNoSynonym = emisCode;
        int index = emisCodeNoSynonym.indexOf("-");
        if (index > -1) {
            emisCodeNoSynonym = emisCodeNoSynonym.substring(0, index);
        }

        //without a Read 2 engine, there seems to be no cast-iron way to determine whether the supplied codes
        //are Read 2 codes or Emis local codes. Looking at the codes from the test data sets, this seems
        //to be a reliable way to perform the same check.
        if (emisCode.startsWith("EMIS")
                || emisCode.startsWith("ALLERGY")
                || emisCode.startsWith("EGTON")
                || emisCodeNoSynonym.length() > 5) {
            system = FhirUri.CODE_SYSTEM_EMIS_CODE;
        }

        //Emis store Read 2 codes without the padding stops, which seems to be against Read 2 standards,
        //so make sure all codes are padded to five chars
        if (system == FhirUri.CODE_SYSTEM_READ2) {
            while (emisCode.length() < 5) {
                emisCode += ".";
            }
        }

        CodeableConcept fhirConcept = CodeableConceptHelper.createCodeableConcept(system, emisTerm, emisCode);

        //if we also have a mapped code, then add that to the codeable concept
        String mappedCode = code.getMapCode();
        if (StringUtils.isNotBlank(mappedCode)) {
            String mappedTerm = emisTerm;
            system = getCodingSystem(code.getMapScheme());

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

    private static String getCodingSystem(String scheme) throws TransformException {
        if (scheme.equals(EMISOPEN_IDENTIFIER_READ2)) {
            return FhirUri.CODE_SYSTEM_READ2;

        } else if (scheme.equals(EMISOPEN_IDENTIFIER_EMIS_PREPARATION)) {
            return FhirUri.CODE_SYSTEM_EMISPREPARATION;

        } else if (scheme.equals(EMISOPEN_IDENTIFIER_SNOMED)) {
            return FhirUri.CODE_SYSTEM_SNOMED_CT;

        } else if (scheme.equals(EMISOPEN_IDENTIFIER_EMIS_BOTH)) {
            return FhirUri.CODE_SYSTEM_EMIS_CODE;

        } else if (scheme.equals(EMISOPEN_IDENTIFIER_EMIS_DRUG_NAME)) {
            return FhirUri.CODE_SYSTEM_EMIS_CODE;

        } else if (scheme.equals(EMISOPEN_IDENTIFIER_EMIS_DRUG_GROUP)) {
            return FhirUri.CODE_SYSTEM_EMIS_CODE;

        } else if (scheme.equals(EMISOPEN_IDENTIFIER_EMIS_NON_DRUG_ALLERGY)) {
            return FhirUri.CODE_SYSTEM_EMIS_CODE;

        } else if (scheme.equals(EMISOPEN_IDENTIFIER_EMIS_CONSTITUENT)) {
            return FhirUri.CODE_SYSTEM_EMIS_CODE;

        } else {
            //log the scheme out
            throw new TransformException("Coding scheme not recognised " + scheme);
            //throw new TransformException("Coding scheme not recognised");
        }
    }
}
