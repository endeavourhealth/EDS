import {BaseHttp2Service} from "../core/baseHttp2.service";
import {FhirResourceType} from "./models/FhirResourceType";
import {FhirResourceContainer} from "./models/FhirResourceContainer";
import {Injectable} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";

@Injectable()
export class ResourcesService extends BaseHttp2Service {
	constructor(http:Http) { super(http); }

	getAllResourceTypes():Observable<FhirResourceType[]> {
		return this.httpGet('api/resources/allResourceTypes');
	}

	/*getResourceTypesForPatient(patientId : string):Observable<FhirResourceType[]> {
		var request = {
			params: {
				'patientId': patientId
			}
		};

		return this.httpGet('api/resources/resourceTypesForPatient', request);
	}*/

	getResourceForId(resourceType : string, resourceId : string):Observable<FhirResourceContainer[]> {
		var params = new URLSearchParams();
		params.append('resourceType', resourceType);
		params.append('resourceId', resourceId);

		return this.httpGet('api/resources/forId', { search : params });
	}

	getResourcesForPatient(resourceType : string, patientId : string):Observable<FhirResourceContainer[]> {
		var params = new URLSearchParams();
		params.append('resourceType', resourceType);
		params.append('patientId', patientId);

		return this.httpGet('api/resources/forPatient', { search : params });
	}

	getResourcesHistory(resourceType : string, resourceId : string):Observable<FhirResourceContainer[]> {
		var params = new URLSearchParams();
		params.append('resourceType', resourceType);
		params.append('resourceId', resourceId);

		return this.httpGet('api/resources/resourceHistory', { search : params });
	}
}
