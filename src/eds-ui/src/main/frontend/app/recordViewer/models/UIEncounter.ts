import {UIPractitioner} from "./UIPractitioner";
import {UICode} from "./UICode";
import {UIPeriod} from "./UIPeriod";

export class UIEncounter {
    status: string;
    performedBy: UIPractitioner;
    enteredBy: UIPractitioner;
    reason: UICode[];
    period: UIPeriod;
}
