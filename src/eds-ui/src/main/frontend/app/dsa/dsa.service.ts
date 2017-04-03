import {Injectable} from "@angular/core";
import {URLSearchParams, Http} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {Dsa} from "./models/Dsa";
import {DataFlow} from "../dataFlow/models/DataFlow";

@Injectable()
export class DsaService extends BaseHttp2Service  {
    constructor(http : Http) { super (http); }

    getAllDsas(): Observable<Dsa[]> {
        return this.httpGet('api/dsa');
    }

    getDsa(uuid : string) : Observable<Dsa> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/dsa', { search : params });
    }

    saveDsa(dsa : Dsa) : Observable<any> {
        return this.httpPost('api/dsa', dsa);
    }

    deleteDsa(uuid : string) : Observable<any> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpDelete('api/dsa', { search : params });
    }

    search(searchData : string) : Observable<Dsa[]> {
        let params = new URLSearchParams();
        params.set('searchData',searchData);
        return this.httpGet('api/dsa', { search : params });
    }

    getLinkedDataFlows(uuid : string) :  Observable<DataFlow[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/dsa/dataflows', { search : params });
    }
}
