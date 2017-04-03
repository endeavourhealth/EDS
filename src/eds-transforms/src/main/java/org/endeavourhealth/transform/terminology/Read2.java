package org.endeavourhealth.transform.terminology;

public class Read2 {


    private static final String ROOT_PREVENTATIVE_PROCEDURES = "6";
    private static final String ROOT_OPERATIONS_PROCEDURES = "7";
    private static final String ROOT_OTHER_THERAPUTIC_PROCEDURES = "8";

    private static final String ROOT_INFECTIOUS_AND_PARASITIC_DIEASES = "A";
    private static final String ROOT_NEOPLASMS = "B";
    private static final String ROOT_ENDOCRINE_NUTRITIONAL_METABOLIC_IMMUNITY_DISORDERS = "C";
    private static final String ROOT_DISEASES_OF_BLOOD = "D";
    private static final String ROOT_MENTAL_DISORDERS = "E";
    private static final String ROOT_NERVOUS_SYSTEM_AND_SENSE_ORGAN_DISEASES = "F";
    private static final String ROOT_CIRCULATORY_SYSTEM_DISEASES = "G";
    private static final String ROOT_RESPIRATORY_SYSTEM_DISEASES = "H";
    private static final String ROOT_DIGESTIVE_SYSTEM_DISEASES = "J";
    private static final String ROOT_GENITOURINARY_SYSTEM_DISEASES = "K";
    private static final String ROOT_SKIN_AND_SUBCUTANEOUS_TISSUE_DISEASES = "M";
    private static final String ROOT_MUSCULOSKELETAL_AND_CONNECTIVE_TISSUE_DISEASES = "N";

    public static boolean isProcedure(String code) {
        return code.startsWith(ROOT_OPERATIONS_PROCEDURES)
                || code.startsWith(ROOT_OTHER_THERAPUTIC_PROCEDURES)
                || code.startsWith(ROOT_PREVENTATIVE_PROCEDURES);
    }

    public static boolean isDisorder(String code) {
        return code.startsWith(ROOT_INFECTIOUS_AND_PARASITIC_DIEASES)
                || code.startsWith(ROOT_NEOPLASMS)
                || code.startsWith(ROOT_ENDOCRINE_NUTRITIONAL_METABOLIC_IMMUNITY_DISORDERS)
                || code.startsWith(ROOT_DISEASES_OF_BLOOD)
                || code.startsWith(ROOT_MENTAL_DISORDERS)
                || code.startsWith(ROOT_NERVOUS_SYSTEM_AND_SENSE_ORGAN_DISEASES)
                || code.startsWith(ROOT_CIRCULATORY_SYSTEM_DISEASES)
                || code.startsWith(ROOT_RESPIRATORY_SYSTEM_DISEASES)
                || code.startsWith(ROOT_DIGESTIVE_SYSTEM_DISEASES)
                || code.startsWith(ROOT_GENITOURINARY_SYSTEM_DISEASES)
                || code.startsWith(ROOT_SKIN_AND_SUBCUTANEOUS_TISSUE_DISEASES)
                || code.startsWith(ROOT_MUSCULOSKELETAL_AND_CONNECTIVE_TISSUE_DISEASES);
    }


}
