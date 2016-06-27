module app.models {
	'use strict';

	export class Protocol {
		enabled : string;
		patientConsent : string;
		cohort : string;
		dataSet : string;
		serviceContract : ServiceContract[];

	}
}