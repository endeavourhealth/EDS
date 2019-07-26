import {BaseHttp2Service} from "eds-common-js";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {SftpReaderChannelStatus} from "./SftpReaderChannelStatus";
import {SftpReaderInstance} from "./SftpReaderInstance";

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
}
