import {BaseHttpService} from "./baseHttp.service";
import {OrganisationSetMember} from "../models/OrganisationSet/OrganisationSetMember";
import {OrganisationSet} from "../models/OrganisationSet/OrganisationSet";

export interface IOrganisationSetService {
	getOrganisationSets():ng.IPromise<OrganisationSet[]>;
	getOrganisationSetMembers(uuid : string):ng.IPromise<OrganisationSetMember[]>;
	searchOrganisations(searchCriteria : string):ng.IPromise<OrganisationSetMember[]>;
	saveOrganisationSet(organisationSet : OrganisationSet):ng.IPromise<OrganisationSet>;
	deleteOrganisationSet(organisationSet : OrganisationSet):ng.IPromise<void>;
}

export class OrganisationSetService extends BaseHttpService implements IOrganisationSetService {

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
