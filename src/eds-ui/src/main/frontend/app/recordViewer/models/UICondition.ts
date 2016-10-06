import {UIPeriod} from "./UIPeriod";
import {UIPractitioner} from "./UIPractitioner";
import {UICode} from "./UICode";
import {UICodeableConcept} from "./UICodeableConcept";

export class UICondition {
    status: string;
    performedBy: UIPractitioner;
    enteredBy: UIPractitioner;
    code: UICodeableConcept;
    period: UIPeriod;
    significance: UICode;
}
