import {Service} from "../../models/Service";
import {Organisation} from "../../models/Organisation";
import {System} from "../../models/System";
import {BaseHttpService} from "../../core/baseHttp.service";

export interface IServiceService {
	getAll(): ng.IPromise<Service[]>;
	get(uuid : string) : ng.IPromise<Service>;
	save(service : Service):any;
	delete(uuid : string) : any;
	search(searchData : string) : ng.IPromise<Service[]>;
	getServiceOrganisations(uuid : string) : ng.IPromise<Organisation[]>;
	getSystemsForService(uuid : string) : ng.IPromise<System[]>;
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
		return this.httpDelete('api/service/', request);
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

	getSystemsForService(serviceId : string) : ng.IPromise<System[]> {
		var request = {
			params: {
				'serviceId': serviceId
			}
		};
		return this.httpGet('api/service/systemsForService', request);
	}
}
