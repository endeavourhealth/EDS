import {BaseHttp2Service} from "eds-common-js";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {SftpReaderChannelStatus} from "./SftpReaderChannelStatus";

@Injectable()
export class SftpReaderService extends BaseHttp2Service {

    constructor(http : Http) {
        super (http);
    }

    getSftpReaderStatus(includeInactiveChannels: boolean) : Observable<SftpReaderChannelStatus[]> {
        console.log('Getting SFTP Reader status');

        var params = new URLSearchParams();
        params.append('includeInactiveChannels', '' + includeInactiveChannels);

        return this.httpGet('api/sftpReader/status', { search : params});
    }
}
