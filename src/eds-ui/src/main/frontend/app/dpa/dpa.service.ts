import {Injectable} from "@angular/core";
import {URLSearchParams, Http} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "eds-common-js";
import {Dpa} from "./models/Dpa";
import {DataFlow} from "../dataFlow/models/DataFlow";
import {Cohort} from "../cohort/models/Cohort";
import {DataSet} from "../dataSet/models/Dataset";

@Injectable()
export class DpaService extends BaseHttp2Service  {
    constructor(http : Http) { super (http); }

    getAllDpas(): Observable<Dpa[]> {
        return this.httpGet('api/dpa');
    }

    getDpa(uuid : string) : Observable<Dpa> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/dpa', { search : params });
    }

    saveDpa(dpa : Dpa) : Observable<any> {
        return this.httpPost('api/dpa', dpa);
    }

    deleteDpa(uuid : string) : Observable<any> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpDelete('api/dpa', { search : params });
    }

    search(searchData : string) : Observable<Dpa[]> {
        let params = new URLSearchParams();
        params.set('searchData',searchData);
        return this.httpGet('api/dpa', { search : params });
    }

    getLinkedDataFlows(uuid : string) :  Observable<DataFlow[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/dpa/dataflows', { search : params });
    }

    getLinkedCohorts(uuid : string) :  Observable<Cohort[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/dpa/cohorts', { search : params });
    }

    getLinkedDataSets(uuid : string) :  Observable<DataSet[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/dpa/datasets', { search : params });
    }
}
