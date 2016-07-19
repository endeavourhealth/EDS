package org.endeavourhealth.transform.terminology;

public class Read2 {


    private static final String ROOT_OTHER_THEAPUTIC_PROCEDURES = "8";
    private static final String ROOT_OPERATIONS_PROCEDURES = "7";
    private static final String ROOT_PREVENTATIVE_PROCEDURES = "6";

    public static boolean isProcedure(String code) {
        return code.startsWith(ROOT_OPERATIONS_PROCEDURES)
                || code.startsWith(ROOT_OTHER_THEAPUTIC_PROCEDURES)
                || code.startsWith(ROOT_PREVENTATIVE_PROCEDURES);
    }
}
