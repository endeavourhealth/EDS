import {ServiceContract} from "./ServiceContract";

export class Protocol {
	enabled : string;
	patientConsent : string;
	cohort : string;
	cohortOdsCode: string[];
	dataSet : string;
	serviceContract : ServiceContract[];
}
