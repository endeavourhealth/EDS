import {Injectable} from "@angular/core";
import {URLSearchParams, Http} from "@angular/http";
import {BaseHttp2Service} from "eds-common-js";
import {Observable} from "rxjs";
import {Cohort} from "./models/Cohort";
import {EdsLibraryItem} from "../edsLibrary/models/EdsLibraryItem";

@Injectable()
export class ProtocolService extends BaseHttp2Service {
	constructor(http : Http) { super(http); }

	getProtocols(serviceId: string): Observable<EdsLibraryItem[]> {
		let params = new URLSearchParams();
		params.set('serviceId', serviceId);

		return this.httpGet('api/library/getProtocols', {search: params});
	}

	getCohorts(): Observable<Cohort[]> {
		return this.httpGet('api/library/getQueries');
	}
}