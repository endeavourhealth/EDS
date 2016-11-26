import {UIPractitioner} from "../admin/UIPractitioner";
import {UIResource} from "../UIResource";
import {UIDate} from "../../types/UIDate";
import {UIMedication} from "./UIMedication";

export class UIMedicationOrder extends UIResource {
    dateAuthorized: UIDate;
    prescriber: UIPractitioner;
    medication : UIMedication;
}
