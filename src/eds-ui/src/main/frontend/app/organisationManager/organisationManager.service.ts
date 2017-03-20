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

    getAllServices(): Observable<Organisation[]> {
        let params = new URLSearchParams();
        params.set('searchType','services');
        return this.httpGet('api/organisationManager', { search : params });
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

    getChildOrganisations(uuid : string) :  Observable<Organisation[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/organisationManager/childOrganisations', { search : params });
    }

    getParentOrganisations(uuid : string) :  Observable<Organisation[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/organisationManager/parentOrganisations', { search : params });
    }

    getServices(uuid : string) :  Observable<Organisation[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/organisationManager/services', { search : params });
    }

    getOrganisationAddresses(uuid : string) :  Observable<Address[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/organisationManager/addresses', { search : params });
    }

    saveOrganisation(organisation : Organisation) : Observable<any> {
        console.log(organisation);
        return this.httpPost('api/organisationManager', organisation);
    }

    deleteOrganisation(uuid : string) : Observable<any> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpDelete('api/organisationManager', { search : params });
    }

    search(searchData : string, searchType : string) : Observable<Organisation[]> {
        let params = new URLSearchParams();
        params.set('searchData',searchData);
        params.set('searchType',searchType);
        return this.httpGet('api/organisationManager', { search : params });
    }

    getOrganisationMarkers(uuid : string) : Observable<Marker[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/organisationManager/markers', { search : params });
    }


}
