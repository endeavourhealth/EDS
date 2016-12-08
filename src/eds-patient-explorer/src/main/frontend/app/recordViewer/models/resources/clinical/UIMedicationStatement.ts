import {UIPractitioner} from "../admin/UIPractitioner";
import {UIResource} from "../UIResource";
import {UIDate} from "../../types/UIDate";
import {UIMedication} from "./UIMedication";
import {UIQuantity} from "../../types/UIQuantity";
import {UICode} from "../../types/UICode";

export class UIMedicationStatement extends UIResource {
    dateAuthorized: UIDate;
    prescriber: UIPractitioner;
    medication : UIMedication;
    dosage: string;
    status: string;
    mostRecentIssue: UIDate;
    authorizationType : UICode;
    authorizedQuantity: UIQuantity;
    repeatsAllowed: number;
    repeatsIssued: number;
}
