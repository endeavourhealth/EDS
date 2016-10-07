import {UICodeableConcept} from "../recordViewer/models/types/UICodeableConcept";

export function codeTerm() {
    return getCodeTerm;
}

function getCodeTerm(codeableConcept: UICodeableConcept) {
    if (codeableConcept == null)
        return "";

    for (let code of codeableConcept.codes)
        if (code.system == "http://snomed.info/sct")
            return code.display;

    return "no snomed term";
}
