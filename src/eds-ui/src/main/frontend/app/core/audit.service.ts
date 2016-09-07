/// <reference path="../../typings/index.d.ts" />

module app.core {
	import AuditEvent = app.models.AuditEvent;
	'use strict';

	export interface IAuditService {
		getAuditEvents(serviceId : string):ng.IPromise<AuditEvent[]>;
	}

	export class AuditService extends BaseHttpService implements IAuditService {

		getAuditEvents(serviceId : string):ng.IPromise<AuditEvent[]> {
			var request = {
				params: {
					'serviceId': serviceId,
				}
			};

			return this.httpGet('api/audit/getAuditEvents', request);
		}
	}

	angular
		.module('app.core')
		.service('AuditService', AuditService);
}