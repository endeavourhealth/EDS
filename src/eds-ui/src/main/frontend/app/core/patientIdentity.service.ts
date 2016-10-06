import {BaseHttpService} from "./baseHttp.service";
import {PatientIdentity} from "../models/PatientIdentity";

export interface IPatientIdentityService {
	getByLocalIdentifier(serviceId : string, systemId : string, localId : string):ng.IPromise<PatientIdentity[]>;
	getByNhsNumber(nhsNumber : string):ng.IPromise<PatientIdentity[]>
	getByPatientId(patientId : string):ng.IPromise<PatientIdentity[]>
}

export class PatientIdentityService extends BaseHttpService implements IPatientIdentityService {

	getByLocalIdentifier(serviceId : string, systemId : string, localId : string):ng.IPromise<PatientIdentity[]> {
		var request = {
			params: {
				'serviceId': serviceId,
				'systemId': systemId,
				'localId': localId
			}
		};

		return this.httpGet('api/patientIdentity/byLocalIdentifier', request);
	}

	getByNhsNumber(nhsNumber : string):ng.IPromise<PatientIdentity[]> {
		var request = {
			params: {
				'nhsNumber': nhsNumber
			}
		};

		return this.httpGet('api/patientIdentity/byNhsNumber', request);
	}

	getByPatientId(patientId : string):ng.IPromise<PatientIdentity[]> {
		var request = {
			params: {
				'patientId': patientId
			}
		};

		return this.httpGet('api/patientIdentity/byPatientId', request);
	}
}
