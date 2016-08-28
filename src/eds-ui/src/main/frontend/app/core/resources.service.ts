/// <reference path="../../typings/index.d.ts" />

module app.core {
	import FhirResourceType = app.models.FhirResourceType;
	import FhirResourceContainer = app.models.FhirResourceContainer;
	'use strict';

	export interface IResourcesService {
		getAllResourceTypes():ng.IPromise<FhirResourceType[]>;
		getResourceTypesForPatient(patientId : string):ng.IPromise<FhirResourceType[]>
		getResourceForId(resourceType : string, resourceId : string):ng.IPromise<FhirResourceContainer[]>;
		getResourcesForPatient(resourceType : string, patientId : string):ng.IPromise<FhirResourceContainer[]>;
		getResourcesHistory(resourceType : string, resourceId : string):ng.IPromise<FhirResourceContainer[]>;
	}

	export class ResourcesService extends BaseHttpService implements IResourcesService {

		getAllResourceTypes():ng.IPromise<FhirResourceType[]> {
			return this.httpGet('api/resources/allResourceTypes');
		}

		getResourceTypesForPatient(patientId : string):ng.IPromise<FhirResourceType[]> {
			var request = {
				params: {
					'patientId': patientId
				}
			};

			return this.httpGet('api/resources/resourceTypesForPatient');
		}

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

	angular
		.module('app.core')
		.service('ResourcesService', ResourcesService);
}