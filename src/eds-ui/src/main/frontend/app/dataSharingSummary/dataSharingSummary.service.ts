import {Injectable} from "@angular/core";
import {URLSearchParams, Http} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {DataSharingSummary} from "./models/DataSharingSummary";
import {OrganisationManagerStatistics} from "../organisationManager/models/OrganisationManagerStatistics";

@Injectable()
export class DataSharingSummaryService extends BaseHttp2Service  {
    constructor(http : Http) { super (http); }

    getAllDataSharingSummaries(): Observable<DataSharingSummary[]> {
        return this.httpGet('api/dataSharingSummary');
    }

    getDataSharingSummary(uuid : string) : Observable<DataSharingSummary> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpGet('api/dataSharingSummary', { search : params });
    }

    saveDataSharingSummary(cohort : DataSharingSummary) : Observable<any> {
        return this.httpPost('api/dataSharingSummary', cohort);
    }

    deleteDataSharingSummary(uuid : string) : Observable<any> {
        let params = new URLSearchParams();
        params.set('uuid',uuid);
        return this.httpDelete('api/dataSharingSummary', { search : params });
    }

    search(searchData : string) : Observable<DataSharingSummary[]> {
        let params = new URLSearchParams();
        params.set('searchData',searchData);
        return this.httpGet('api/dataSharingSummary', { search : params });
    }

    getStatistics(type : string) : Observable<OrganisationManagerStatistics[]> {
        let params = new URLSearchParams();
        params.set('type',type);
        return this.httpGet('api/dataSharingSummary/statistics', { search : params });
    }
}
