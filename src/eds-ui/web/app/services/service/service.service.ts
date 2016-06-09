/// <reference path="../../../typings/tsd.d.ts" />

module app.service {
	import BaseHttpService = app.core.BaseHttpService;
	'use strict';

	export interface IServiceService {
		getAll(): ng.IPromise<Service[]>;
		get(uuid : string) : ng.IPromise<Service>;
		save(organisation : Service):any;
		delete(uuid : string) : any;
	}

	export class ServiceService extends BaseHttpService implements IServiceService {

		getAll(): ng.IPromise<Service[]> {
			return this.httpGet('api/service');
		}

		get(uuid : string) : ng.IPromise<Service> {
			var request = {
				params: {
					'uuid': uuid
				}
			};
			return this.httpGet('api/service', request);
		}

		save(service : Service) {
			return this.httpPost('api/service', service);
		}

		delete(uuid : string) {
			var request = {
				params: {
					'uuid': uuid
				}
			};

			return this.httpDelete('api/service', request);
		}
	}

	angular
		.module('app.service')
		.service('ServiceService', ServiceService);
}