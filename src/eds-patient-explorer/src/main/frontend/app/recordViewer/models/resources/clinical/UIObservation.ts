import {UIClinicalResource} from "./UIClinicalResource";
import {UIEncounter} from "./UIEncounter";
import {UIQuantity} from "../../types/UIQuantity";

export class UIObservation extends UIClinicalResource {
    private status: string;
    private value: UIQuantity;
    private referenceRangeLow: UIQuantity;
    private referenceRangeHigh: UIQuantity;
    private encounter: UIEncounter;
}
