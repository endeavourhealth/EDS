import {BaseHttpService} from "./baseHttp.service";
import {PatientService} from "../models/PatientService";

export interface IMedicalRecordService {
    getServices() : ng.IPromise<PatientService[]>;
}

export class MedicalRecordService extends BaseHttpService implements IMedicalRecordService {
    getServices(): ng.IPromise<PatientService[]> {
        return this.httpGet('api/medicalRecord/getServices');
    }
}
