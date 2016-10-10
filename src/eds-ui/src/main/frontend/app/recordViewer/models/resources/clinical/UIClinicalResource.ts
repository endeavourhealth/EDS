import {UICodeableConcept} from "../../types/UICodeableConcept";
import {UIDate} from "../../types/UIDate";
import {UIPractitioner} from "../admin/UIPractitioner";
import {UIResource} from "../UIResource";

export class UIClinicalResource extends UIResource {
    code: UICodeableConcept;
    effectivePractitioner: UIPractitioner;
    effectiveDate: UIDate;
    recordingPractitioner: UIPractitioner;
    recordedDate: UIDate;
    notes: String;
}