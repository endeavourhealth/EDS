import {Http, URLSearchParams} from "@angular/http";
import {Injectable} from "@angular/core";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {PatientIdentity} from "./PatientIdentity";
import {Observable} from "rxjs";
import {Service} from "./models/Service";
import {System} from "./models/System";

@Injectable()
export class PatientIdentityService extends BaseHttp2Service {
	constructor (http:Http) { super(http); }

	getServices(): Observable<Service[]> {
		return this.httpGet('api/service');
	}

	getSystemsForService(serviceId : string) : Observable<System[]> {
		let params = new URLSearchParams();
		params.set('serviceId',serviceId);
		return this.httpGet('api/service/systemsForService', { search : params });
	}

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
