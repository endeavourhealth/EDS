import {Http, URLSearchParams} from "@angular/http";
import {Injectable} from "@angular/core";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {PatientIdentity} from "./PatientIdentity";
import {Observable} from "rxjs";

@Injectable()
export class PatientIdentityService extends BaseHttp2Service {
	constructor (http:Http) { super(http); }

	getByLocalIdentifier(serviceId : string, systemId : string, localId : string):Observable<PatientIdentity[]> {
		var params = new URLSearchParams();
		params.append('serviceId', serviceId);
		params.append('systemId', systemId);
		params.append('localId', localId);

		return this.httpGet('api/patientIdentity/byLocalIdentifier', { search : params });
	}

	getByNhsNumber(nhsNumber : string):Observable<PatientIdentity[]> {
		var params = new URLSearchParams();
		params.append('nhsNumber', nhsNumber);

		return this.httpGet('api/patientIdentity/byNhsNumber', { search : params });
	}

	getByPatientId(patientId : string):Observable<PatientIdentity[]> {
		var params = new URLSearchParams();
		params.append('patientId', patientId);

		return this.httpGet('api/patientIdentity/byPatientId', { search : params });
	}
}
