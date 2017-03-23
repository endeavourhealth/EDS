import {UIPatient} from "./models/resources/admin/UIPatient";
import {UIEncounter} from "./models/resources/clinical/UIEncounter";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {UIProblem} from "./models/resources/clinical/UIProblem";
import {UIInternalIdentifier} from "./models/UIInternalIdentifier";
import {UIDiary} from "./models/resources/clinical/UIDiary";
import {UIService} from "./models/UIService";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {URLSearchParams, Http} from "@angular/http";
import {UIAllergy} from "./models/resources/clinical/UIAllergy";
import {UIImmunisation} from "./models/resources/clinical/UIImmunisation";
import {UIFamilyHistory} from "./models/resources/clinical/UIFamilyHistory";
import {UIMedicationStatement} from "./models/resources/clinical/UIMedicationStatement";
import {UIPerson} from "./models/resources/admin/UIPerson";
import {UIEpisodeOfCare} from "./models/resources/clinical/UIEpisodeOfCare";

@Injectable()
export class RecordViewerService extends BaseHttp2Service {
    constructor(http: Http) {
        super(http);
    }

    getServices(): Observable<UIService[]> {
        return this.httpGet('api/recordViewer/getServices');
    }

    getEpisodes(person : UIPerson) : Observable<UIEpisodeOfCare[]> {
        var params = new URLSearchParams();
        params.append('nhsNumber', person.nhsNumber);

        return this.httpGet('api/recordViewer/getEpisodes', { search : params });
    }

    findPatient(service: UIService, searchTerms: string): Observable<UIPatient[]> {
        var params = new URLSearchParams();
        params.append('serviceId', service.serviceId);
        params.append('systemId', service.systemId);
        params.append('searchTerms', searchTerms);

        return this.httpGet('api/recordViewer/findPatient', {search: params});
    }

    findPerson(searchTerms: string): Observable<UIPatient[]> {
        var params = new URLSearchParams();
        params.append('searchTerms', searchTerms);

        return this.httpGet('api/recordViewer/findPerson', {search: params});
    }

    getPatient(patientId: UIInternalIdentifier): Observable<UIPatient> {
        return this.httpGet('api/recordViewer/getPatient', this.getParams(patientId));
    }

    getEncounters(patientId: UIInternalIdentifier): Observable<UIEncounter[]> {
        return this.httpGet('api/recordViewer/getEncounters', this.getParams(patientId));
    }

    getProblems(patientId: UIInternalIdentifier): Observable<UIProblem[]> {
        return this.httpGet('api/recordViewer/getProblems', this.getParams(patientId));
    }

    getDiary(patientId: UIInternalIdentifier): Observable<UIDiary[]> {
        return this.httpGet('api/recordViewer/getDiary', this.getParams(patientId));
    }

    getObservations(patientId: UIInternalIdentifier): Observable<UIDiary[]> {
        return this.httpGet('api/recordViewer/getObservations', this.getParams(patientId));
    }

    getMedication(patientId : UIInternalIdentifier): Observable<UIMedicationStatement[]> {
        return this.httpGet('api/recordViewer/getMedicationStatements', this.getParams(patientId));
    }

    getAllergies(patientId: UIInternalIdentifier) : Observable<UIAllergy[]> {
        return this.httpGet('api/recordViewer/getAllergies', this.getParams(patientId));
    }

    getImmunisations(patientId: UIInternalIdentifier) : Observable<UIImmunisation[]> {
        return this.httpGet('api/recordViewer/getImmunisations', this.getParams(patientId));
    }

    getFamilyHistory(patientId: UIInternalIdentifier) : Observable<UIFamilyHistory[]> {
        return this.httpGet('api/recordViewer/getFamilyHistory', this.getParams(patientId));
    }

    private getParams(patientId: UIInternalIdentifier): any {
        var params = new URLSearchParams();
        params.append('serviceId', patientId.serviceId);
        params.append('systemId', patientId.systemId);
        params.append('patientId', patientId.resourceId);

        return {search: params};
    }
}
