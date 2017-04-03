import {Injectable} from "@angular/core";
import {URLSearchParams, Http} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {DataFlow} from "./models/DataFlow";
import {Dpa} from "../dpa/models/Dpa";
import {Dsa} from "../dsa/models/Dsa";

@Injectable()
export class DataFlowService extends BaseHttp2Service  {
    constructor(http : Http) { super (http); }

    getAllDataFlows(): Observable<DataFlow[]> {
        return this.httpGet('api/dataFlow');
    }

    getDataFlow(uuid : string) : Observable<DataFlow> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/dataFlow', { search : params });
    }

    saveDataFlow(cohort : DataFlow) : Observable<any> {
        return this.httpPost('api/dataFlow', cohort);
    }

    deleteDataFlow(uuid : string) : Observable<any> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpDelete('api/dataFlow', { search : params });
    }

    search(searchData : string) : Observable<DataFlow[]> {
        let params = new URLSearchParams();
        params.set('searchData',searchData);
        return this.httpGet('api/dataFlow', { search : params });
    }

    getLinkedDpas(uuid : string) :  Observable<Dpa[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/dataFlow/dpas', { search : params });
    }

    getLinkedDsas(uuid : string) :  Observable<Dsa[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/dataFlow/dsas', { search : params });
    }
}
