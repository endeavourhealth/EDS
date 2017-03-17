import {Injectable} from "@angular/core";
import {URLSearchParams, Http} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {Dpa} from "./models/Dpa";

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

    saveDpa(dsa : Dpa) : Observable<any> {
        return this.httpPost('api/dpa', dsa);
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
}
