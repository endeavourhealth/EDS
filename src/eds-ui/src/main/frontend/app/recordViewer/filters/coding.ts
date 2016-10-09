import {UICodeableConcept} from "../models/types/UICodeableConcept";
import {UICode} from "../models/types/UICode";

export function codeTerm() {
    return getCodeTerm;
}

export function codeSignificance() {
    return getCodeSignificance;
}

function getCodeTerm(codeableConcept: UICodeableConcept): string {
    if (codeableConcept == null)
        return "";

    for (let code of codeableConcept.codes)
        if (code.system == "http://endeavourhealth.org/fhir/read2")
            return code.display;

    return "no snomed term";
}

function getCodeSignificance(significance: UICode): string {
    if (significance == null)
        return "";

    switch (significance.code) {
        case "386134007": return "(significant)";
        case "371928007": return "(minor)";
        default: return "";
    }
}