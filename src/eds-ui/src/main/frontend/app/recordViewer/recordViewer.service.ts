/// <reference path="../../typings/index.d.ts" />

module app.core {
	import Patient = app.models.Patient;

	'use strict';

	export interface IRecordViewerService {
        findPatient(searchTerms: string): ng.IPromise<Patient[]>;
		getDemographics(serviceId: string, systemId: string, patientId: string): ng.IPromise<Patient>;
	}

	export class RecordViewerService extends BaseHttpService implements IRecordViewerService {

        findPatient(searchTerms: string): ng.IPromise<Patient[]> {
            var request = {
                params: {
                    'searchTerms': searchTerms
                }
            }

            return this.httpGet('api/recordViewer/findPatient', request);
        }

		getDemographics(serviceId: string, systemId: string, patientId: string): ng.IPromise<Patient> {
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