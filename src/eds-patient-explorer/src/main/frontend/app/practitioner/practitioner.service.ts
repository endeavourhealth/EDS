import {Observable} from "rxjs";
import {Http, URLSearchParams} from "@angular/http";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {Injectable} from "@angular/core";
import {Practitioner} from "./models/Practitioner";

@Injectable()
export class PractitionerService extends BaseHttp2Service {
	constructor(http: Http) {
		super(http);
	}

	search(searchData : string, organisationUuid : string) : Observable<Practitioner[]> {
		var params = new URLSearchParams();
		params.append('searchData', searchData);
		params.append('organisationUuid', organisationUuid);

		return this.httpGet('api/practitioner/search', {search: params});
	}
}
