import {BaseHttpService} from "../core/baseHttp.service";
import {PatientService} from "../models/PatientService";

export class MedicalRecordService extends BaseHttpService {
    getServices(): ng.IPromise<PatientService[]> {
        return this.httpGet('api/medicalRecord/getServices');
    }
}
