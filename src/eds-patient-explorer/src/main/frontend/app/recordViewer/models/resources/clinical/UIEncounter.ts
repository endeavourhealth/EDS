import {UIPractitioner} from "../admin/UIPractitioner";
import {UICode} from "../../types/UICode";
import {UIPeriod} from "../../types/UIPeriod";
import {UIResource} from "../UIResource";
import {UIOrganisation} from "../admin/UIOrganisation";
import {UICodeableConcept} from "../../types/UICodeableConcept";

export class UIEncounter extends UIResource {
    status: string;
    performedBy: UIPractitioner;
    enteredBy: UIPractitioner;
    reason: UICodeableConcept[];
    period: UIPeriod;
    serviceProvider : UIOrganisation;
    encounterSource : UICodeableConcept;
}
