/// <reference path="../../typings/index.d.ts" />

module app.core {
	import Demographics = app.models.Demographics;
	'use strict';

	export interface IRecordViewerService {
		getDemographics(serviceId : string, systemId : string, patientId : string):ng.IPromise<Demographics>;
	}

	export class RecordViewerService extends BaseHttpService implements IRecordViewerService {

		getDemographics(serviceId : string, systemId : string, patientId : string):ng.IPromise<Demographics> {
			var request = {
				params: {
					'serviceId': serviceId,
					'systemId': systemId,
					'patientId': patientId
				}
			};

			return this.httpGet('api/recordViewer/getDemographics', request);
		}
	}

	angular
		.module('app.core')
		.service('RecordViewerService', RecordViewerService);
}