import {BaseHttp2Service} from "eds-common-js";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {QueueReaderStatus} from "./queueReaderStatus";


@Injectable()
export class QueueReaderStatusService extends BaseHttp2Service {

    constructor(http : Http) {
        super (http);
    }

    getStatus() : Observable<QueueReaderStatus[]> {

        var params = new URLSearchParams();

        return this.httpGet('api/queueReader/status', { search: params });
    }

}
