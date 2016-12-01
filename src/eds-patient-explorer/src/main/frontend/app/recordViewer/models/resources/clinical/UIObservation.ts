import {UIClinicalResource} from "./UIClinicalResource";
import {UIEncounter} from "./UIEncounter";
import {UIQuantity} from "../../types/UIQuantity";
import {UIObservationRelation} from "./UIObservationRelation";

export class UIObservation extends UIClinicalResource {
    status: string;
    value: UIQuantity;
    referenceRangeLow: UIQuantity;
    referenceRangeHigh: UIQuantity;
    encounter: UIEncounter;
    related : UIObservationRelation[];
}
