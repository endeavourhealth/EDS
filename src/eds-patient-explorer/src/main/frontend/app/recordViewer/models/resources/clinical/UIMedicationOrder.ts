import {UIPractitioner} from "../admin/UIPractitioner";
import {UIResource} from "../UIResource";
import {UIDate} from "../../types/UIDate";
import {UIMedication} from "./UIMedication";
import {UIDosageInstruction} from "./UIDosageInstruction";

export class UIMedicationOrder extends UIResource {
    dateAuthorized: UIDate;
    dateEnded: UIDate;
    prescriber: UIPractitioner;
    medication : UIMedication;
    dosageInstructions: UIDosageInstruction[];
}
