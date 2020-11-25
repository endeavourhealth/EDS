import {BaseHttp2Service} from "eds-common-js";
import {Injectable} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {SubscriberZipFileUUID} from "./models/SubscriberZipFileUUID";
import {RemoteFilingStatistics} from "./models/RemoteFilingStatistics";
import {RemoteFilingSubscribers} from "./models/RemoteFilingSubscribers";

@Injectable()
export class RemoteFilingService extends BaseHttp2Service {
    constructor (http : Http) { super (http); }

    getPagedFiles(pageNumber: number, pageSize: number):Observable<SubscriberZipFileUUID[]> {
        const vm = this;
        const params = new URLSearchParams();
        params.set('pageNumber',pageNumber.toString());
        params.set('pageSize',pageSize.toString());
        return this.httpGet('api/remoteFiling/getAllRemoteFilingHistoryPaged', { search : params });
    }

    getSubscriberPagedFiles(subscriberId: number, pageNumber: number, pageSize: number):Observable<SubscriberZipFileUUID[]> {
        const vm = this;
        const params = new URLSearchParams();
        params.set('subscriberId',subscriberId.toString());
        params.set('pageNumber',pageNumber.toString());
        params.set('pageSize',pageSize.toString());

        return this.httpGet('api/remoteFiling/getSubscriberRemoteFilingHistoryPaged', { search : params });
    }

    getRemoteFilingCount(): Observable<number> {
        return this.httpGet('api/remoteFiling/getRemoteFilingCount');
    }

    getRemoteSubscriberFilingCount(subscriberId: number): Observable<number> {
        const vm = this;
        const params = new URLSearchParams();
        params.set('subscriberId',subscriberId.toString());

        return this.httpGet('api/remoteFiling/getRemoteSubscriberFilingCount', { search : params });
    }

    getStatistics(timeframe: string):Observable<RemoteFilingStatistics[]> {
        let params = new URLSearchParams();
        params.set('timeframe',timeframe);

        return this.httpGet('api/remoteFiling/getStatistics', { search : params });
    }

    getSubscriberStatistics(subscriberId: number, timeFrame: string):Observable<RemoteFilingStatistics[]> {
        let params = new URLSearchParams();
        params.set('subscriberId', subscriberId.toString());
        params.set('timeFrame', timeFrame);

        return this.httpGet('api/remoteFiling/getSubscriberStatistics', { search : params });
    }

    getSubscribers():Observable<RemoteFilingSubscribers[]> {

        return this.httpGet('api/remoteFiling/getSubscribers');
    }

    getFailedFiles(subscriberId: number, timeFrame: string):Observable<SubscriberZipFileUUID[]> {
        const vm = this;
        const params = new URLSearchParams();
        params.set('subscriberId', subscriberId.toString());
        params.set('timeFrame', timeFrame);
        return this.httpGet('api/remoteFiling/getFailedFilingUUIDEntities', { search : params });
    }
}
