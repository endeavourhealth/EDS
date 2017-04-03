import {Injectable} from "@angular/core";
import {URLSearchParams, Http} from "@angular/http";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {Observable} from "rxjs";
import {LibraryItem} from "../library/models/LibraryItem";
import {Cohort} from "./models/Cohort";

@Injectable()
export class ProtocolService extends BaseHttp2Service {
	constructor(http : Http) { super(http); }

	getProtocols(serviceId: string): Observable<LibraryItem[]> {
		let params = new URLSearchParams();
		params.set('serviceId', serviceId);

		return this.httpGet('api/library/getProtocols', {search: params});
	}

	getCohorts(): Observable<Cohort[]> {
		return this.httpGet('api/library/getQueries');
	}
}