import {UIPeriod} from "../../types/UIPeriod";
import {UIPractitioner} from "../admin/UIPractitioner";
import {UICode} from "../../types/UICode";
import {UICodeableConcept} from "../../types/UICodeableConcept";
import {UIClinicalResource} from "./UIClinicalResource";

export class UICondition extends UIClinicalResource {
    status: string;
    performedBy: UIPractitioner;
    enteredBy: UIPractitioner;
    code: UICodeableConcept;
    period: UIPeriod;
    significance: UICode;
}
