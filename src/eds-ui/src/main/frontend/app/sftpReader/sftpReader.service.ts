import {BaseHttp2Service} from "eds-common-js";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {SftpReaderChannelStatus} from "./SftpReaderChannelStatus";
import {SftpReaderInstance} from "./SftpReaderInstance";
import {SftpReaderChannelBatch} from "./SftpReaderChannelBatch";

@Injectable()
export class SftpReaderService extends BaseHttp2Service {

    constructor(http : Http) {
        super (http);
    }

    getSftpReaderStatus(filterInstanceName: string) : Observable<SftpReaderChannelStatus[]> {
        var params = new URLSearchParams();
        params.append('instance', '' + filterInstanceName);

        return this.httpGet('api/sftpReader/status', { search : params});
    }

    getSftpReaderInstances() : Observable<SftpReaderInstance[]> {
        return this.httpGet('api/sftpReader/instances', { });
    }

    getSftpReaderHistory(configurationId: string, dFrom: Date, dTo: Date) : Observable<SftpReaderChannelBatch[]> {
        var params = new URLSearchParams();
        params.append('configurationId', '' + configurationId);
        params.append('from', '' + dFrom.getTime());
        params.append('to', '' + dTo.getTime());

        return this.httpGet('api/sftpReader/history', { search : params});
    }
}
