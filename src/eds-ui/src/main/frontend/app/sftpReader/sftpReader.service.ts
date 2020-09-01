import {BaseHttp2Service} from "eds-common-js";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {SftpReaderChannelStatus} from "./models/SftpReaderChannelStatus";
import {SftpReaderChannelBatch} from "./models/SftpReaderChannelBatch";
import {SftpReaderConfiguration} from "./models/SftpReaderConfiguration";

@Injectable()
export class SftpReaderService extends BaseHttp2Service {

    constructor(http : Http) {
        super (http);
    }

    getSftpReaderStatus(configurationId: string) : Observable<SftpReaderChannelStatus> {
        var params = new URLSearchParams();
        params.append('configurationId', '' + configurationId);

        return this.httpGet('api/sftpReader/status', { search : params});
    }

    getSftpReaderInstances() : Observable<SftpReaderConfiguration[]> {
        return this.httpGet('api/sftpReader/instances', { });
    }

    getSftpReaderHistory(configurationId: string, dFrom: Date, dTo: Date) : Observable<SftpReaderChannelBatch[]> {
        var params = new URLSearchParams();
        params.append('configurationId', '' + configurationId);
        params.append('from', '' + dFrom.getTime());
        params.append('to', '' + dTo.getTime());

        return this.httpGet('api/sftpReader/history', { search : params});
    }

    ignoreBatchSplit(batchId: number, batchSplitId: number, configurationId: string, reason: string) : Observable<any> {
        var request = {
            'batchId': batchId,
            'batchSplitId': batchSplitId,
            'configurationId': configurationId,
            'reason': reason
        };
        return this.httpPost('api/sftpReader/ignore', request);
    }

    togglePause(configurationId: string) : Observable<any> {
        var request = {
            'configurationId': configurationId
        };
        return this.httpPost('api/sftpReader/togglePause', request);
    }

    togglePauseAll() : Observable<any> {
        var request = {
        };
        return this.httpPost('api/sftpReader/togglePauseAll', request);
    }
}
