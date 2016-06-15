module app.models {
	'use strict';

	export class Protocol {
		enabled : string;
		patientConsent : string;
		dataSet : string;
		serviceContract : ServiceContract[];

	}
}