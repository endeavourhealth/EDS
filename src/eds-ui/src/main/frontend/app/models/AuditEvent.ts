module app.models {
	'use strict';

	export class AuditEvent {
		userId : string;
		serviceId : string;
		module : string;
		subModule : string;
		action : string;
		timestamp : string;
		data : string;
	}
}