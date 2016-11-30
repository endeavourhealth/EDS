import {Injectable} from "@angular/core";
import {URLSearchParams, Http} from "@angular/http";
import {Observable} from "rxjs";

import {BaseHttp2Service} from "../core/baseHttp2.service";
import {Organisation} from "./models/Organisation";
import {Service} from "../services/models/Service";

@Injectable()
export class OrganisationService extends BaseHttp2Service  {
	constructor(http : Http) { super (http); }

	getOrganisations(): Observable<Organisation[]> {
		return this.httpGet('api/organisation');
	}

	getOrganisation(uuid : string) : Observable<Organisation> {
		let params = new URLSearchParams();
		params.set('uuid',uuid);
		return this.httpGet('api/organisation', { search : params });
	}

	getOrganisationServices(uuid : string) :  Observable<Service[]> {
		let params = new URLSearchParams();
		params.set('uuid',uuid);
		return this.httpGet('api/organisation/services', { search : params });
	}

	saveOrganisation(organisation : Organisation) : Observable<any> {
		return this.httpPost('api/organisation', organisation);
	}

	deleteOrganisation(uuid : string) : Observable<any> {
		let params = new URLSearchParams();
		params.set('uuid',uuid);
		return this.httpDelete('api/organisation', { search : params });
	}

	search(searchData : string) : Observable<Organisation[]> {
		let params = new URLSearchParams();
		params.set('searchData',searchData);
		return this.httpGet('api/organisation', { search : params });
	}
}
