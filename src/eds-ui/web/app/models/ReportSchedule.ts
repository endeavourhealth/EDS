module app.models {
	'use strict';

	export class ReportSchedule {
		uuid : string;
		date : string;
		status : string;
		endUserUuid : string;
		endUserName : string;
		parameters : RequestParameters;
	}
}