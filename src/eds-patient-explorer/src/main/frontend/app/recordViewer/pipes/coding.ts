import {UICodeableConcept} from "../models/types/UICodeableConcept";
import {UICode} from "../models/types/UICode";
import {Pipe, PipeTransform} from "@angular/core";

@Pipe({name : 'codeReadTerm'})
export class CodeReadTerm implements PipeTransform {
    transform(codeableConcept: UICodeableConcept): string {
        let code: UICode = getReadUICode(codeableConcept)

        if (code == null)
            return "(no term available)";

        return code.display;
    }
}

@Pipe({name : 'codeReadCode'})
export class CodeReadCode implements PipeTransform {
    transform(codeableConcept: UICodeableConcept): string {
        let code: UICode = getReadUICode(codeableConcept)

        if (code == null)
            return "";

        return code.code;
    }
}

@Pipe({name : 'codeSnomedCode'})
export class CodeSnomedCode implements PipeTransform {
    transform(codeableConcept: UICodeableConcept): string {
        return getSnomedCode(codeableConcept);
    }
}

@Pipe({name : 'codeSnomedLink'})
export class CodeSnomedLink implements PipeTransform {
    transform(codeableConcept: UICodeableConcept): string {
        let code: string = getSnomedCode(codeableConcept);

        if (code == "")
            return "";

        return "http://browser.ihtsdotools.org/?perspective=full&conceptId1="
          + code + "&edition=uk-edition&release=v20161001&server=https://browser-aws-1.ihtsdotools.org/api/snomed&langRefset=900000000000508004";
    }
}

@Pipe({name : 'codeSignificance'})
export class CodeSignificance implements PipeTransform {
    transform(significance: UICode): string {
        return getCodeSignificance(significance);
    }
}

function getSnomedCode(codeableConcept: UICodeableConcept): string {
    let code: UICode = getSnomedUICode(codeableConcept)

    if (code == null)
        return "";

    return code.code;
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

function getCodeSignificance(significance: UICode): string {
    if (significance == null)
        return "";

    switch (significance.code) {
        case "386134007": return "Significant";
        case "371928007": return "Minor";
        default: return "";
    }
}
//
// export function hasDisplayableCodeSignificance(significance: UICode): boolean {
//     return (getCodeSignificance(significance) != '');
// }
