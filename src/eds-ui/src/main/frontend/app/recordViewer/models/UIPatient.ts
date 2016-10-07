import {UIAddress} from "./UIAddress";
import {UIHumanName} from "./UIHumanName";
import {UIDate} from "./UIDate";

export class UIPatient {
	serviceId: string;
	systemId: string;
	patientId: string;
	nhsNumber: string;
	name: UIHumanName;
	dateOfBirth: UIDate;
	gender: string;
	homeAddress: UIAddress;
}
