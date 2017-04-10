import {Injectable} from "@angular/core";
import {URLSearchParams, Http} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "eds-common-js";
import {Organisation} from "../organisationManager/models/Organisation";
import {Region} from "./models/Region";
import {Dsa} from "../dsa/models/Dsa";

@Injectable()
export class RegionService extends BaseHttp2Service  {
    constructor(http : Http) { super (http); }

    getAllRegions(): Observable<Region[]> {
        return this.httpGet('api/region');
    }

    getRegion(uuid : string) : Observable<Region> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/region', { search : params });
    }

    getRegionOrganisations(uuid : string) :  Observable<Organisation[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/region/organisations', { search : params });
    }

    getParentRegions(uuid : string) :  Observable<Region[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/region/parentRegions', { search : params });
    }

    getChildRegions(uuid : string) :  Observable<Region[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/region/childRegions', { search : params });
    }

    getSharingAgreements(uuid : string) :  Observable<Dsa[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/region/sharingAgreements', { search : params });
    }

    saveRegion(region : Region) : Observable<any> {
        return this.httpPost('api/region', region);
    }

    deleteRegion(uuid : string) : Observable<any> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpDelete('api/region', { search : params });
    }

    search(searchData : string) : Observable<Region[]> {
        let params = new URLSearchParams();
        params.set('searchData',searchData);
        return this.httpGet('api/region', { search : params });
    }
}
