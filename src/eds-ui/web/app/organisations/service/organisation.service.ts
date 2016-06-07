/// <reference path="../../../typings/tsd.d.ts" />

module app.organisation {
	import BaseHttpService = app.core.BaseHttpService;
	'use strict';

	export interface IOrganisationService {
		getOrganisations(): ng.IPromise<Organisation[]>;
		getOrganisation(uuid : string) : ng.IPromise<Organisation>;
		saveOrganisation(organisation : Organisation):any;
		deleteOrganisation(uuid : string) : any;
	}

	export class OrganisationService extends BaseHttpService implements IOrganisationService {

		getOrganisations(): ng.IPromise<Organisation[]> {
			return this.httpGet('api/organisation');
		}

		getOrganisation(uuid : string) : ng.IPromise<Organisation> {
			var request = {
				params: {
					'uuid': uuid
				}
			};
			return this.httpGet('api/organisation', request);
		}

		saveOrganisation(organisation : Organisation) {
			return this.httpPost('api/organisation', organisation);
		}

		deleteOrganisation(uuid : string) {
			var request = {
				params: {
					'uuid': uuid
				}
			};

			return this.httpDelete('api/organisation', request);
		}
	}

	angular
		.module('app.organisation')
		.service('OrganisationService', OrganisationService);
}