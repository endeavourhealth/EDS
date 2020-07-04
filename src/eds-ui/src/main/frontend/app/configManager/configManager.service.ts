import {BaseHttp2Service} from "eds-common-js";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {ConfigRecord} from "./ConfigRecord";
import {ConfigHistory} from "./ConfigHistory";

@Injectable()
export class ConfigManagerService extends BaseHttp2Service {

    //put these here so they don't get reset when changing pages
    filterAppId: string;
    filterAppIdIncludeGlobal: boolean;
    filterSearchText: string;
    filterSearchOnAppId: boolean;
    filterSearchOnConfigId: boolean;
    filterSearchOnData: boolean;

    constructor(http : Http) {
        super (http);

        var vm = this;
        vm.filterAppIdIncludeGlobal = false;
        vm.filterSearchOnConfigId = true;
    }

    getRecords() : Observable<ConfigRecord[]> {
        return this.httpGet('api/configManager/records', {});
    }

    getHistory(appId: string, configId: string) : Observable<ConfigHistory[]> {
        let params = new URLSearchParams();
        params.set('appId', appId);
        params.set('configId', configId);
        return this.httpGet('api/configManager/history', { search : params });
    }

    saveRecord(record: ConfigRecord) : Observable<{}> {
        return this.httpPost('api/configManager/saveRecord', record);
    }

    deleteRecord(record: ConfigRecord) : Observable<{}> {
        return this.httpPost('api/configManager/deleteRecord', record);
    }
}
