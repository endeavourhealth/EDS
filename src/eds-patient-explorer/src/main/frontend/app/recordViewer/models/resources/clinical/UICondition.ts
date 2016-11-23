import {UIClinicalResource} from "./UIClinicalResource";
import {UIEncounter} from "./UIEncounter";
import {UIDate} from "../../types/UIDate";
import {UIProblem} from "./UIProblem";

export class UICondition extends UIClinicalResource {
    encounter: UIEncounter;
    clinicalStatus: string;
    verificationStatus: string;
    abatementDate: UIDate;
    hasAbated: boolean;
    partOfProblem: UIProblem;
}
