/// <reference path="../../typings/index.d.ts" />

module app.core {
	import Patient = app.models.Patient;
    import Encounter = app.models.Encounter;

	'use strict';

	export interface IRecordViewerService {
        findPatient(searchTerms: string): ng.IPromise<Patient[]>;
		getPatient(serviceId: string, systemId: string, patientId: string): ng.IPromise<Patient>;
        getEncounters(serviceId: string, systemId: string, patientId: string): ng.IPromise<Encounter[]>;
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

		getPatient(serviceId: string, systemId: string, patientId: string): ng.IPromise<Patient> {
			var request = {
				params: {
					'serviceId': serviceId,
					'systemId': systemId,
					'patientId': patientId
				}
			};

			return this.httpGet('api/recordViewer/getPatient', request);
		}

		getEncounters(serviceId: string, systemId: string, patientId: string): ng.IPromise<Encounter[]> {
            var request = {
                params: {
                    'serviceId': serviceId,
                    'systemId': systemId,
                    'patientId': patientId
                }
            };

            return this.httpGet('api/recordViewer/getEncounters', request);
        }
	}

	angular
		.module('app.core')
		.service('RecordViewerService', RecordViewerService);
}