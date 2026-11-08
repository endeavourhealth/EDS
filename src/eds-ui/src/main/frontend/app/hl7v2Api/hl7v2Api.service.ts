import {BaseHttp2Service} from "eds-common-js";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {Hl7v2ApiStatus} from "./Hl7v2ApiStatus";

@Injectable()
export class Hl7v2ApiService extends BaseHttp2Service {

    constructor(http : Http) {
        super (http);
    }

    getHl7ReceiverStatus() : Observable<Hl7v2ApiStatus[]> {
        return this.httpGet('api/hl7v2Api/status', {});
    }


}
