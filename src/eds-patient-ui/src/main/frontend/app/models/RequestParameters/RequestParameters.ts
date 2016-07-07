module app.models {
	'use strict';

	export class RequestParameters {
		reportUuid : string;
		baselineDate : string;
		patientType : string;
		patientStatus : string;
		organisation : string[];
	}
}