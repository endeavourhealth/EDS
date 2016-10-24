import {Injectable} from "@angular/core";
import {Http} from "@angular/http";
import {PatientService} from "../models/PatientService";
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {BaseHttp2Service} from "../core/baseHttp2.service";

@Injectable()
export class MedicalRecordService extends BaseHttp2Service {
    services : PatientService[];

    constructor(http : Http) { super (http); }

    getServices() : Observable<PatientService[]> {
      return this.httpGet('api/medicalRecord/getServices');
    }
}
