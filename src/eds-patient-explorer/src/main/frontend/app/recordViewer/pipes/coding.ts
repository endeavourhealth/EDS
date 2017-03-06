import {UICode} from "../models/types/UICode";
import {Pipe, PipeTransform} from "@angular/core";

@Pipe({name : 'codeSignificance'})
export class CodeSignificance implements PipeTransform {
    transform(significance: UICode): string {
        return getCodeSignificance(significance);
    }
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

