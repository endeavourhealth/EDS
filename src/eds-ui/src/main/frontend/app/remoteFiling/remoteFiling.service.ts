import {BaseHttp2Service} from "eds-common-js";
import {Injectable} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {SubscriberZipFileUUID} from "./models/SubscriberZipFileUUID";
import {RemoteFilingStatistics} from "./models/RemoteFilingStatistics";

@Injectable()
export class RemoteFilingService extends BaseHttp2Service {
    constructor (http : Http) { super (http); }

    getPagedFiles(pageNumber: number, pageSize: number):Observable<SubscriberZipFileUUID[]> {
        const vm = this;
        const params = new URLSearchParams();
        params.set('pageNumber',pageNumber.toString());
        params.set('pageSize',pageSize.toString());
        return this.httpGet('api/remoteFiling/getPagedRemoteFilingStatus', { search : params });
    }

    getRemoteFilingCount(): Observable<number> {
        return this.httpGet('api/remoteFiling/getRemoteFilingCount');
    }

    getStatistics(timeframe: string):Observable<RemoteFilingStatistics[]> {
        let params = new URLSearchParams();
        params.set('timeframe',timeframe);

        return this.httpGet('api/remoteFiling/getStatistics', { search : params });
    }
}
