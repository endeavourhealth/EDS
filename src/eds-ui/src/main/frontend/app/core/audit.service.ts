/// <reference path="../../typings/index.d.ts" />

module app.core {
	import AuditEvent = app.models.AuditEvent;
	import User = app.models.User;
	'use strict';

	export interface IAuditService {
		getAuditEvents(userId : string, organisationId : string, module : string, submodule : string, action : string):ng.IPromise<AuditEvent[]>;
		getUsers() : ng.IPromise<User[]>;
		getModules() : ng.IPromise<string[]>;
		getSubmodules(module : string) : ng.IPromise<string[]>;
		getActions() : ng.IPromise<string[]>;
	}

	export class AuditService extends BaseHttpService implements IAuditService {

		getUsers():ng.IPromise<User[]> {
			return this.httpGet('api/audit/users');
		}

		getAuditEvents(userId : string, organisationId : string, module : string, submodule : string, action : string):ng.IPromise<AuditEvent[]> {
			var request = {
				params: {
					'userId' : userId,
					'organisationId': organisationId,
					'module' : module,
					'subModule' : submodule,
					'action' : action
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