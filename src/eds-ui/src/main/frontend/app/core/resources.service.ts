/// <reference path="../../typings/index.d.ts" />

module app.core {
	'use strict';

	export interface IResourcesService {
		getResource(resourceType : string, resourceId : string):ng.IPromise<{}>;
		getResourcesHistory(resourceType : string, resourceId : string):ng.IPromise<{}>;
		getResourcesForPatient(patientId : string, resourceType : string):ng.IPromise<{}>;
	}

	export class ResourcesService extends BaseHttpService implements IResourcesService {

		getResource(resourceType : string, resourceId : string):ng.IPromise<{}> {
			var request = {
				params: {
					'resourceType': resourceType,
					'resourceId': resourceId
				}
			};

			return this.httpGet('api/resources/forId', request);
		}

		getResourcesHistory(resourceType : string, resourceId : string):ng.IPromise<{}> {
			var request = {
				params: {
					'resourceType': resourceType,
					'resourceId': resourceId
				}
			};

			return this.httpGet('api/resources/resourceHistory', request);
		}

		getResourcesForPatient(patientId : string, resourceType : string):ng.IPromise<{}> {
			var request = {
				params: {
					'patientId': patientId,
					'resourceType': resourceType
				}
			};

			return this.httpGet('api/resources/forPatient', request);
		}
	}

	angular
		.module('app.core')
		.service('ResourcesService', ResourcesService);
}