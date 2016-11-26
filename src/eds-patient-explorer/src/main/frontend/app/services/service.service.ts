import {Injectable} from "@angular/core";
import {Observable} from "rxjs";

import {Service} from "./models/Service";
import {Organisation} from "../organisations/models/Organisation";
import {System} from "../systems/models/System";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {Http, URLSearchParams} from "@angular/http";

@Injectable()
export class ServiceService extends BaseHttp2Service {
	constructor(http : Http) { super (http); }

	getAll(): Observable<Service[]> {
		return this.httpGet('api/service');
	}

	get(uuid : string) : Observable<Service> {
		let params = new URLSearchParams();
		params.set('uuid',uuid);
		return this.httpGet('api/service', { search : params });
	}

	save(service : Service) : Observable<any> {
		return this.httpPost('api/service', service);
	}

	delete(uuid : string) : Observable<any> {
		let params = new URLSearchParams();
		params.set('uuid',uuid);
		return this.httpDelete('api/service/', { search : params });
	}

	search(searchData : string) : Observable<Service[]> {
		let params = new URLSearchParams();
		params.set('searchData',searchData);
		return this.httpGet('api/service', { search : params });
	}

	getServiceOrganisations(uuid : string) : Observable<Organisation[]> {
		let params = new URLSearchParams();
		params.set('uuid',uuid);
		return this.httpGet('api/service/organisations', { search : params });
	}

	getSystemsForService(serviceId : string) : Observable<System[]> {
		let params = new URLSearchParams();
		params.set('serviceId',serviceId);
		return this.httpGet('api/service/systemsForService', { search : params });
	}
}

