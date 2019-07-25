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

    getSftpReaderStatus() : Observable<SftpReaderChannelStatus[]> {
        console.log('Getting SFTP Reader status');
        return this.httpGet('api/sftpReader/status', {});
    }
}
