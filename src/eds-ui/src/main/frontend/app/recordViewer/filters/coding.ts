import {UICodeableConcept} from "../models/types/UICodeableConcept";
import {UICode} from "../models/types/UICode";

export function codeReadTerm() {
    return getReadCodeTerm;
}

export function codeReadCode() {
    return getReadCode;
}

export function codeSnomedCode() {
    return getSnomedCode;
}

export function codeSnomedCodeWithLink() {
    return getSnomedCodeWithLink;
}

export function codeSignificance() {
    return getCodeSignificance;
}

function getReadCodeTerm(codeableConcept: UICodeableConcept): string {
    let code: UICode = getReadUICode(codeableConcept)

    if (code == null)
        return "(no term available)";

    return code.display;
}

function getSnomedUICode(codeableConcept: UICodeableConcept): UICode {
    return getCode(codeableConcept, "http://snomed.info/sct");
}

function getReadUICode(codeableConcept: UICodeableConcept): UICode {
    return getCode(codeableConcept, "http://endeavourhealth.org/fhir/read2");
}

function getCode(codeableConcept: UICodeableConcept, system: string): UICode {
    if (codeableConcept == null)
        return null;

    for (let code of codeableConcept.codes)
        if (code.system == system)
            return code;

    return null;
}

function getSnomedCode(codeableConcept: UICodeableConcept): string {
    let code: UICode = getSnomedUICode(codeableConcept)

    if (code == null)
        return "";

    return code.code;
}


function getSnomedCodeWithLink(codeableConcept: UICodeableConcept): string {
    let code: string = getSnomedCode(codeableConcept);

    if (code == "")
        return "";

    return "<a href=\"http://browser.ihtsdotools.org/?perspective=full&conceptId1="
            + code
            + "&edition=uk-edition&release=v20161001&server=https://browser-aws-1.ihtsdotools.org/api/snomed&langRefset=900000000000508004\" target=\"_blank\">"
            + code
            + "</a>";
}

function getReadCode(codeableConcept: UICodeableConcept): string {
    let code: UICode = getReadUICode(codeableConcept)

    if (code == null)
        return "";

    return code.code;
}

function getCodeSignificance(significance: UICode): string {
    if (significance == null)
        return "";

    switch (significance.code) {
        case "386134007": return "Significant";
        case "371928007": return "Minor";
        default: return "";
    }
}

export function hasDisplayableCodeSignificance(significance: UICode): boolean {
    return (getCodeSignificance(significance) != '');
}
