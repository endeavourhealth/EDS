import {Injectable} from "@angular/core";
import {URLSearchParams, Http} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "eds-common-js";
import {Organisation} from "./models/Organisation";
import {Region} from "../region/models/Region";
import {Address} from "./models/Address";
import {Marker} from "../region/models/Marker";
import {OrganisationManagerStatistics} from "./models/OrganisationManagerStatistics";
import {FileUpload} from "./models/FileUpload";

@Injectable()
export class OrganisationManagerService extends BaseHttp2Service  {

    constructor(http : Http) { super (http); }

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

    getParentOrganisations(uuid : string, isService : number) :  Observable<Organisation[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        params.set('isService', isService.toString());
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
        return this.httpPost('api/organisationManager', organisation);
    }

    deleteOrganisation(uuid : string) : Observable<any> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpDelete('api/organisationManager', { search : params });
    }

    search(searchData : string, searchType : string,
           pageNumber : number = 1, pageSize : number = 20,
            orderColumn : string = 'name', descending : boolean = false ) : Observable<Organisation[]> {
        let params = new URLSearchParams();
        params.set('searchData',searchData);
        params.set('searchType',searchType);
        params.set('pageNumber', pageNumber.toString());
        params.set('pageSize', pageSize.toString());
        params.set('orderColumn', orderColumn);
        params.set('descending', descending.toString());
        return this.httpGet('api/organisationManager', { search : params });
    }

    getOrganisationMarkers(uuid : string) : Observable<Marker[]> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/organisationManager/markers', { search : params });
    }

    getUpdatedBulkOrganisations() : Observable<any> {
        return this.httpGet('api/organisationManager/editedBulks');
    }

    getConflictedOrganisations() : Observable<any> {
        return this.httpGet('api/organisationManager/conflicts');
    }

    deleteBulks() : Observable<any> {
        return this.httpDelete('api/organisationManager/deleteBulks');
    }

    startUpload() : Observable<any> {
        return this.httpGet('api/organisationManager/startUpload');
    }

    endUpload() : Observable<any> {
        return this.httpGet('api/organisationManager/endUpload');
    }

    uploadCsv(fileToUpload : FileUpload) : Observable<any> {
        return this.httpPost('api/organisationManager/upload', fileToUpload);
    }

    getOrganisationStatistics() : Observable<OrganisationManagerStatistics[]> {
        return this.httpGet('api/organisationManager/organisationStatistics');
    }

    getServiceStatistics() : Observable<OrganisationManagerStatistics[]> {
        return this.httpGet('api/organisationManager/serviceStatistics');
    }

    getRegionStatistics() : Observable<OrganisationManagerStatistics[]> {
        return this.httpGet('api/organisationManager/regionStatistics');
    }

    getTotalCount(expression: string, searchType: string) : Observable<number> {
        const params = new URLSearchParams();
        params.set('expression', expression);
        params.set('searchType', searchType);
        return this.httpGet('api/organisationManager/searchCount', { search : params });
    }

}
