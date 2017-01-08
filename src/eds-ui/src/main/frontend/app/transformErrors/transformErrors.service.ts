import {BaseHttp2Service} from "../core/baseHttp2.service";
import {TransformErrorSummary} from "./TransformErrorSummary";
import {TransformErrorDetail} from "./TransformErrorDetail";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";

@Injectable()
export class TransformErrorsService extends BaseHttp2Service {
    constructor(http:Http) { super(http); }


    getTransformErrorSummaries():Observable<TransformErrorSummary[]> {
        return this.httpGet('api/transformErrors/getErrorSummaries');
    }

    getTransformErrorDetail(serviceId:string, systemId:string, exchangeId:string):Observable<TransformErrorDetail> {
        var params = new URLSearchParams();
        params.append('serviceId', serviceId);
        params.append('systemId', systemId);
        params.append('exchangeId', exchangeId);

        return this.httpGet('api/transformErrors/getErrorDetails', { search : params});
    }

    rerunFirst(serviceId: string, systemId: string):Observable<TransformErrorSummary> {
        var request = {
            'serviceId': serviceId,
            'systemId': systemId
        };
        return this.httpPost('api/transformErrors/rerunFirstExchange', request);
        /*return this.httpPost('api/transformErrors/rerunFirstExchange', { body : request });*/
    }

    rerunAll(serviceId: string, systemId: string) : Observable<any> {
        var request = {
            'serviceId': serviceId,
            'systemId': systemId
        };
        return this.httpPost('api/transformErrors/rerunAllExchanges', request);
        /*return this.httpPost('api/transformErrors/rerunAllExchanges', { body : request });*/
    }

}
