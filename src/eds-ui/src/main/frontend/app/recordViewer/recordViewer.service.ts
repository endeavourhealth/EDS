import {UIPatient} from "./models/resources/admin/UIPatient";
import {UIEncounter} from "./models/resources/clinical/UIEncounter";
import {UICondition} from "./models/resources/clinical/UICondition";
import {BaseHttpService} from "../core/baseHttp.service";

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
