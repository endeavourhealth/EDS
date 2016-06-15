/// <reference path="../../../typings/tsd.d.ts" />

module app.service {
	import BaseHttpService = app.core.BaseHttpService;
	import Service = app.models.Service;
	import Organisation = app.models.Organisation;
	'use strict';

	export interface IServiceService {
		getAll(): ng.IPromise<Service[]>;
		get(uuid : string) : ng.IPromise<Service>;
		save(service : Service):any;
		delete(uuid : string) : any;
		search(searchData : string) : ng.IPromise<Service[]>;
		getServiceOrganisations(uuid : string) : ng.IPromise<Organisation[]>;
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

		search(searchData : string) : ng.IPromise<Service[]> {
			var request = {
				params: {
					'searchData': searchData
				}
			}

			return this.httpGet('api/service', request);
		}

		getServiceOrganisations(uuid : string) :  ng.IPromise<Organisation[]> {
			var request = {
				params: {
					'uuid': uuid
				}
			};
			return this.httpGet('api/service/organisations', request);
		}

	}

	angular
		.module('app.service')
		.service('ServiceService', ServiceService);
}