import {BaseHttp2Service} from "eds-common-js";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {Hl7ReceiverChannelStatus} from "./Hl7ReceiverChannelStatus";

@Injectable()
export class Hl7ReceiverService extends BaseHttp2Service {

    constructor(http : Http) {
        super (http);
    }

    getHl7ReceiverStatus() : Observable<Hl7ReceiverChannelStatus[]> {
        console.log('Getting HL7 Receiver status');
        return this.httpGet('api/hl7Receiver/channelStatus', {});
    }

    pauseChannel(channelId: number, pause: boolean) : Observable<{}> {
        console.log('Toggle paused status for ' + channelId);

        return this.httpPost('api/hl7Receiver/pause/' + channelId + '/' + pause, {});
    }
}
