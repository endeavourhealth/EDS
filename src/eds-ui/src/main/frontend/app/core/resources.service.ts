import {BaseHttpService} from "./baseHttp.service";
import {FhirResourceType} from "../models/FhirResourceType";
import {FhirResourceContainer} from "../models/FhirResourceContainer";

export interface IResourcesService {
	getAllResourceTypes():ng.IPromise<FhirResourceType[]>;
	//getResourceTypesForPatient(patientId : string):ng.IPromise<FhirResourceType[]>
	getResourceForId(resourceType : string, resourceId : string):ng.IPromise<FhirResourceContainer[]>;
	getResourcesForPatient(resourceType : string, patientId : string):ng.IPromise<FhirResourceContainer[]>;
	getResourcesHistory(resourceType : string, resourceId : string):ng.IPromise<FhirResourceContainer[]>;
}

export class ResourcesService extends BaseHttpService implements IResourcesService {

	getAllResourceTypes():ng.IPromise<FhirResourceType[]> {
		return this.httpGet('api/resources/allResourceTypes');
	}

	/*getResourceTypesForPatient(patientId : string):ng.IPromise<FhirResourceType[]> {
		var request = {
			params: {
				'patientId': patientId
			}
		};

		return this.httpGet('api/resources/resourceTypesForPatient', request);
	}*/

	getResourceForId(resourceType : string, resourceId : string):ng.IPromise<FhirResourceContainer[]> {
		var request = {
			params: {
				'resourceType': resourceType,
				'resourceId': resourceId
			}
		};

		return this.httpGet('api/resources/forId', request);
	}

	getResourcesForPatient(resourceType : string, patientId : string):ng.IPromise<FhirResourceContainer[]> {
		var request = {
			params: {
				'resourceType': resourceType,
				'patientId': patientId
			}
		};

		return this.httpGet('api/resources/forPatient', request);
	}

	getResourcesHistory(resourceType : string, resourceId : string):ng.IPromise<FhirResourceContainer[]> {
		var request = {
			params: {
				'resourceType': resourceType,
				'resourceId': resourceId
			}
		};

		return this.httpGet('api/resources/resourceHistory', request);
	}
}
