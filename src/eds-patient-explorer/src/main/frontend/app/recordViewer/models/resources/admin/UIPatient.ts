import {UIAddress} from "../../types/UIAddress";
import {UIHumanName} from "../../types/UIHumanName";
import {UIDate} from "../../types/UIDate";
import {UIResource} from "../UIResource";
import {UIInternalIdentifier} from "../../UIInternalIdentifier";

export class UIPatient extends UIResource {
	patientId: UIInternalIdentifier;
	nhsNumber: string;
	name: UIHumanName;
	dateOfBirth: UIDate;
	gender: string;
	homeAddress: UIAddress;
}
