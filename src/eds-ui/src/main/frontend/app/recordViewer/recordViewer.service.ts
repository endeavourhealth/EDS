import {UIPatient} from "./models/resources/admin/UIPatient";
import {UIEncounter} from "./models/resources/clinical/UIEncounter";
import {BaseHttpService} from "../core/baseHttp.service";
import {UIProblem} from "./models/resources/clinical/UIProblem";
import {UIInternalIdentifier} from "./models/UIInternalIdentifier";
import {UIDiary} from "./models/resources/clinical/UIDiary";

export interface IRecordViewerService {
    findPatient(searchTerms: string): ng.IPromise<UIPatient[]>;
    getPatient(patientId: UIInternalIdentifier): ng.IPromise<UIPatient>;
    getEncounters(patientId: UIInternalIdentifier): ng.IPromise<UIEncounter[]>;
    getProblems(patientId: UIInternalIdentifier): ng.IPromise<UIProblem[]>;
    getDiary(patientId: UIInternalIdentifier): ng.IPromise<UIDiary[]>;
}

export class RecordViewerService extends BaseHttpService implements IRecordViewerService {

    findPatient(searchTerms: string): ng.IPromise<UIPatient[]> {
        var request = { params: { 'searchTerms': searchTerms } }
        return this.httpGet('api/recordViewer/findPatient', request);
    }

	getPatient(patientId: UIInternalIdentifier): ng.IPromise<UIPatient> {
		return this.httpGet('api/recordViewer/getPatient', this.getParams(patientId));
	}

	getEncounters(patientId: UIInternalIdentifier): ng.IPromise<UIEncounter[]> {
        return this.httpGet('api/recordViewer/getEncounters', this.getParams(patientId));
    }

    getProblems(patientId: UIInternalIdentifier): ng.IPromise<UIProblem[]> {
        return this.httpGet('api/recordViewer/getProblems', this.getParams(patientId));
    }

    getDiary(patientId: UIInternalIdentifier): ng.IPromise<UIDiary[]> {
        return this.httpGet('api/recordViewer/getDiary', this.getParams(patientId));
    }

    private getParams(patientId: UIInternalIdentifier): any {
        var request = {
            params: {
                'serviceId': patientId.serviceId,
                'systemId': patientId.systemId,
                'patientId': patientId.resourceId
            }
        };

        return request;
    }
}
