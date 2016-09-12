/// <reference path="../../typings/index.d.ts" />

module app.core {
	import AuditEvent = app.models.AuditEvent;
	import User = app.models.User;
	'use strict';

	export interface IAuditService {
		getUserAudit(module : string, userId : string, month : Date, organisationId : string):ng.IPromise<AuditEvent[]>;
		getUsers() : ng.IPromise<User[]>;
		getModules() : ng.IPromise<string[]>;
		getSubmodules(module : string) : ng.IPromise<string[]>;
		getActions() : ng.IPromise<string[]>;
	}

	export class AuditService extends BaseHttpService implements IAuditService {

		getUsers():ng.IPromise<User[]> {
			return this.httpGet('api/audit/users');
		}

		getUserAudit(module : string, userId : string, month : Date, organisationId : string):ng.IPromise<AuditEvent[]> {
			var request = {
				params: {
					'module' : module,
					'userId' : userId,
					'month' : month.valueOf(),
					'organisationId': organisationId
				}
			};

			return this.httpGet('api/audit', request);
		}

		getModules():ng.IPromise<String[]> {
			return this.httpGet('api/audit/modules');
		}

		getSubmodules(module : string):ng.IPromise<String[]> {
			var request = {
				params: {
					'module' : module,
				}
			};

			return this.httpGet('api/audit/submodules', request);
		}

		getActions():ng.IPromise<String[]> {
			return this.httpGet('api/audit/actions');
		}
	}

	angular
		.module('app.core')
		.service('AuditService', AuditService);
}