import {Organisation} from "../../models/Organisation";
import {Service} from "../../models/Service";
import {BaseHttpService} from "../../core/baseHttp.service";

export interface IOrganisationService {
	getOrganisations(): ng.IPromise<Organisation[]>;
	getOrganisation(uuid : string) : ng.IPromise<Organisation>;
	getOrganisationServices(uuid : string) : ng.IPromise<Service[]>;
	saveOrganisation(organisation : Organisation):any;
	deleteOrganisation(uuid : string) : any;
	search(searchData : string) : ng.IPromise<Organisation[]>;
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

	getOrganisationServices(uuid : string) :  ng.IPromise<Service[]> {
		var request = {
			params: {
				'uuid': uuid
			}
		};
		return this.httpGet('api/organisation/services', request);
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

	search(searchData : string) : ng.IPromise<Organisation[]> {
		var request = {
			params: {
				'searchData': searchData
			}
		}

		return this.httpGet('api/organisation', request);
	}
}
