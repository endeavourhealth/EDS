import {BaseHttp2Service} from "eds-common-js";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";

@Injectable()
export class FrailtyApiService extends BaseHttp2Service {

    constructor(http : Http) {
        super (http);
    }

    getHl7ReceiverStatus() : Observable<{}> {
        console.log('Getting HL7 Receiver status');

        return this.httpGet('api/hl7Receiver/channelStatus', {});
    }
}
