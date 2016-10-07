import {UIPractitioner} from "../admin/UIPractitioner";
import {UICode} from "../../types/UICode";
import {UIPeriod} from "../../types/UIPeriod";
import {UIResource} from "../../UIResource";

export class UIEncounter extends UIResource {
    status: string;
    performedBy: UIPractitioner;
    enteredBy: UIPractitioner;
    reason: UICode[];
    period: UIPeriod;
}
