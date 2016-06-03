/// <reference path="../../typings/tsd.d.ts" />

module app.core {
	import OrganisationSet = app.models.OrganisationSet;
	import OrganisationSetMember = app.models.OrganisationSetMember;
	'use strict';

	export interface IOrganisationService {
		getOrganisationSets():ng.IPromise<OrganisationSet[]>;
		getOrganisationSetMembers(uuid : string):ng.IPromise<OrganisationSetMember[]>;
		searchOrganisations(searchCriteria : string):ng.IPromise<OrganisationSetMember[]>;
		saveOrganisationSet(organisationSet : OrganisationSet):ng.IPromise<OrganisationSet>;
		deleteOrganisationSet(organisationSet : OrganisationSet):ng.IPromise<void>;
	}

	export class OrganisationService extends BaseHttpService implements IOrganisationService {

		getOrganisationSets():ng.IPromise<OrganisationSet[]> {
			return this.httpGet('api/lookup/getOrganisationSets');
		}

		getOrganisationSetMembers(uuid : string):ng.IPromise<OrganisationSetMember[]> {
			var request = {
				params: {
					'uuid': uuid
				}
			};

			return this.httpGet('api/lookup/getOrganisationSetMembers', request);
		}

		searchOrganisations(searchCriteria : string):ng.IPromise<OrganisationSetMember[]> {
			var request = {
				params: {
					'searchTerm': searchCriteria
				}
			};

			return this.httpGet('api/lookup/searchOrganisations', request);
		}

		saveOrganisationSet(organisationSet : OrganisationSet) {
			return this.httpPost('api/lookup/saveOrganisationSet', organisationSet);
		}

		deleteOrganisationSet(organisationSet : OrganisationSet) {
			return this.httpPost('api/lookup/deleteOrganisationSet', organisationSet);
		}
	}

	angular
		.module('app.core')
		.service('OrganisationService', OrganisationService);
}