import {UIAddress} from "../../types/UIAddress";
import {UIHumanName} from "../../types/UIHumanName";
import {UIDate} from "../../types/UIDate";
import {UIResource} from "../../UIResource";

export class UIPatient extends UIResource {
	serviceId: string;
	systemId: string;
	patientId: string;
	nhsNumber: string;
	name: UIHumanName;
	dateOfBirth: UIDate;
	gender: string;
	homeAddress: UIAddress;
}
