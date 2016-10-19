import {Injectable} from "@angular/core";
import {Http, Response} from "@angular/http";
import {PatientService} from "../models/PatientService";
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class MedicalRecordService {
    services : PatientService[];

    constructor(private http : Http) { }

    getServices() : Observable<PatientService[]> {
      return this.http.get('api/medicalRecord/getServices')
          .map((res:Response) => {
              this.services = res.json();
              return this.services;
          })
          .catch(this.handleError);
    }

    private handleError(error: any): Promise<any> {
        console.error('An error occurred', error);
        return Promise.reject(error.message || error);
    }
}
