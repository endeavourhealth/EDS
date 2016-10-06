import {UIAddress} from "./UIAddress";
import {UIHumanName} from "./UIHumanName";

export class UIPatient {
	serviceId: string;
	systemId: string;
	patientId: string;
	nhsNumber: string;
	name: UIHumanName;
	dateOfBirth: Date;
	gender: string;
	homeAddress: UIAddress;
}
