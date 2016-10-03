/// <reference path="../../typings/index.d.ts" />

module app.core {
	import UIPatient = app.models.UIPatient;
    import UIEncounter = app.models.UIEncounter;
    import UICondition = app.models.UICondition;

	'use strict';

	export interface IRecordViewerService {
        findPatient(searchTerms: string): ng.IPromise<UIPatient[]>;
		getPatient(serviceId: string, systemId: string, patientId: string): ng.IPromise<UIPatient>;
        getEncounters(serviceId: string, systemId: string, patientId: string): ng.IPromise<UIEncounter[]>;
        getConditions(serviceId: string, systemId: string, patientId: string): ng.IPromise<UICondition[]>;
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

        getConditions(serviceId: string, systemId: string, patientId: string): ng.IPromise<UICondition[]> {
            var request = {
                params: {
                    'serviceId': serviceId,
                    'systemId': systemId,
                    'patientId': patientId
                }
            };

            return this.httpGet('api/recordViewer/getConditions', request);
        }
	}

	angular
		.module('app.core')
		.service('RecordViewerService', RecordViewerService);
}