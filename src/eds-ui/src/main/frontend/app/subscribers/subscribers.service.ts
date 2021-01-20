import {BaseHttp2Service} from "eds-common-js";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {SubscriberConfiguration} from "./models/SubscriberConfiguration";

@Injectable()
export class SubscribersService extends BaseHttp2Service {

    //filters for Subscribers page so they don't get reset when leaving and returning
    /*showMemory: boolean;
    showExecutionTime: boolean;
    showJarDate: boolean;
    showStartDate: boolean;
    showWarningsOnly: boolean;
    filterInstanceName: string;*/


    constructor(http : Http) {
        super (http);

        var vm = this;
        /*vm.filterInstanceName = '';
        vm.showWarningsOnly = true;
        vm.showMemory = true;
        vm.showExecutionTime = true;*/
    }

    getSubscribersInstances() : Observable<SubscriberConfiguration[]> {
        return this.httpGet('api/subscribers/subscribers', { });
    }

    /*getSubscribersStatus(configurationId: string) : Observable<SubscribersChannelStatus> {
        var params = new URLSearchParams();
        params.append('configurationId', '' + configurationId);

        return this.httpGet('api/subscribers/status', { search : params});
    }

    getSubscribersInstances() : Observable<SubscribersConfiguration[]> {
        return this.httpGet('api/subscribers/instances', { });
    }

    getSubscribersHistory(configurationId: string, dFrom: Date, dTo: Date) : Observable<SubscribersChannelBatch[]> {
        var params = new URLSearchParams();
        params.append('configurationId', '' + configurationId);
        params.append('from', '' + dFrom.getTime());
        params.append('to', '' + dTo.getTime());

        return this.httpGet('api/subscribers/history', { search : params});
    }

    ignoreBatchSplit(batchId: number, batchSplitId: number, configurationId: string, reason: string) : Observable<any> {
        var request = {
            'batchId': batchId,
            'batchSplitId': batchSplitId,
            'configurationId': configurationId,
            'reason': reason
        };
        return this.httpPost('api/subscribers/ignore', request);
    }

    togglePause(configurationId: string) : Observable<any> {
        var request = {
            'configurationId': configurationId
        };
        return this.httpPost('api/subscribers/togglePause', request);
    }

    togglePauseAll() : Observable<any> {
        var request = {
        };
        return this.httpPost('api/subscribers/togglePauseAll', request);
    }*/
}
