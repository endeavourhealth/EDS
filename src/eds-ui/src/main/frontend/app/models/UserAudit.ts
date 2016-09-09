module app.models {
	'use strict';

	export class UserAudit {
		pageState : string;
		userEvents : AuditEvent[];
	}
}