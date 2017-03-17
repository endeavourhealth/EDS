import {Injectable} from "@angular/core";
import {URLSearchParams, Http} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {Organisation} from "./models/Organisation";
import {Region} from "../region/models/Region";
import {Address} from "./models/Address";
import {Marker} from "../region/models/Marker";

@Injectable()
export class OrganisationManagerService extends BaseHttp2Service  {

    constructor(http : Http) { super (http); }

    getOrganisations(): Observable<Organisation[]> {
        return this.httpGet('api/organisationManager');
    }

    getOrganisation(uuid : string) : Observable<Organisation> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/organisationManager', { search : params });
    }

    getOrganisationRegions(uuid : string) :  Observable<Region[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/organisationManager/regions', { search : params });
    }

    getOrganisationAddresses(uuid : string) :  Observable<Address[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/organisationManager/addresses', { search : params });
    }

    saveOrganisation(organisation : Organisation) : Observable<any> {
        return this.httpPost('api/organisationManager', organisation);
    }

    deleteOrganisation(uuid : string) : Observable<any> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpDelete('api/organisationManager', { search : params });
    }

    search(searchData : string) : Observable<Organisation[]> {
        let params = new URLSearchParams();
        params.set('searchData',searchData);
        return this.httpGet('api/organisationManager', { search : params });
    }

    getOrganisationMarkers(uuid : string) : Observable<Marker[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/organisationManager/markers', { search : params });
    }


}
