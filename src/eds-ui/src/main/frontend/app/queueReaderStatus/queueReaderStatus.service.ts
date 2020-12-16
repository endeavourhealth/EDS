import {BaseHttp2Service} from "eds-common-js";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {QueueReaderStatus} from "./queueReaderStatus";


@Injectable()
export class QueueReaderStatusService extends BaseHttp2Service {

    showMissingQueueReadersOnEmptyQueues: boolean;
    showOdsCode: boolean;
    showDataDate: boolean;
    showPublisherConfig: boolean;
    showExecutionTime: boolean;
    showJarDate: boolean;
    showStartDate: boolean;

    constructor(http : Http) {
        super (http);

        //this.showMissingQueueReadersOnEmptyQueues = true;
        this.showExecutionTime = true;
    }

    getStatus(applicationName: string) : Observable<QueueReaderStatus[]> {

        var params = new URLSearchParams();
        params.append('applicationName', applicationName);

        return this.httpGet('api/queueReader/status', { search: params });
    }

}
