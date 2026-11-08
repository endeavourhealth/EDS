package org.endeavourhealth.ui.terminology;

import org.endeavourhealth.core.xml.QueryDocument.CodeSet;
import org.endeavourhealth.core.xml.QueryDocument.CodingSystem;

import java.util.HashSet;

public final class TerminologyService {

    public static HashSet<String> enumerateConcepts(CodeSet codeSet) {

        CodingSystem codingSystem = codeSet.getCodingSystem();
        if (codingSystem == CodingSystem.EMIS_READ_V_2) {
            throw new RuntimeException("CodingSystem " + codingSystem + " not supported");
        } else if (codingSystem == CodingSystem.DMD) {
            throw new RuntimeException("CodingSystem " + codingSystem + " not supported");
        } else if (codingSystem == CodingSystem.SNOMED_CT) {
            return Snomed.enumerateConcepts(codeSet);
        } else if (codingSystem == CodingSystem.CTV_3) {
            throw new RuntimeException("CodingSystem " + codingSystem + " not supported");
        } else {
            throw new RuntimeException("Unknown codingSystem " + codingSystem);
        }
    }
}
