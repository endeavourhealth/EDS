import {Injectable} from "@angular/core";
import {URLSearchParams, Http} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {Cohort} from "./models/Cohort";
import {Dpa} from "../dpa/models/Dpa";

@Injectable()
export class CohortService extends BaseHttp2Service  {
    constructor(http : Http) { super (http); }

    getAllCohorts(): Observable<Cohort[]> {
        return this.httpGet('api/cohort');
    }

    getCohort(uuid : string) : Observable<Cohort> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/cohort', { search : params });
    }

    saveCohort(cohort : Cohort) : Observable<any> {
        return this.httpPost('api/cohort', cohort);
    }

    deleteCohort(uuid : string) : Observable<any> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpDelete('api/cohort', { search : params });
    }

    search(searchData : string) : Observable<Cohort[]> {
        let params = new URLSearchParams();
        params.set('searchData',searchData);
        return this.httpGet('api/cohort', { search : params });
    }

    getLinkedDpas(uuid : string) :  Observable<Dpa[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/cohort/dpas', { search : params });
    }
}
