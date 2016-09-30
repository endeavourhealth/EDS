/// <reference path="../../typings/index.d.ts" />

module app.core {
	import UIPatient = app.models.UIPatient;
    import UIEncounter = app.models.UIEncounter;

	'use strict';

	export interface IRecordViewerService {
        findPatient(searchTerms: string): ng.IPromise<UIPatient[]>;
		getPatient(serviceId: string, systemId: string, patientId: string): ng.IPromise<UIPatient>;
        getEncounters(serviceId: string, systemId: string, patientId: string): ng.IPromise<UIEncounter[]>;
	}

	export class RecordViewerService extends BaseHttpService implements IRecordViewerService {

        findPatient(searchTerms: string): ng.IPromise<UIPatient[]> {
            var request = {
                params: {
                    'searchTerms': searchTerms
                }
            }

            return this.httpGet('api/recordViewer/findPatient', request);
        }

		getPatient(serviceId: string, systemId: string, patientId: string): ng.IPromise<UIPatient> {
			var request = {
				params: {
					'serviceId': serviceId,
					'systemId': systemId,
					'patientId': patientId
				}
			};

			return this.httpGet('api/recordViewer/getPatient', request);
		}

		getEncounters(serviceId: string, systemId: string, patientId: string): ng.IPromise<UIEncounter[]> {
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