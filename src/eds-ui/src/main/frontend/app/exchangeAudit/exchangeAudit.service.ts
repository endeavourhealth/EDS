import {BaseHttp2Service} from "../core/baseHttp2.service";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {Exchange} from "./Exchange";
import {ExchangeEvent} from "./ExchangeEvent";

@Injectable()
export class ExchangeAuditService extends BaseHttp2Service {
    constructor(http:Http) { super(http); }


    /*createMissingData() : Observable<any>{
        return this.httpPost('api/exchangeAudit/createMissingData', {});
    }*/

    getExchangeList(serviceId:string, maxRows:number) : Observable<Exchange[]> {
        var params = new URLSearchParams();
        console.log('Getting for service id ' + serviceId + ' and ' + maxRows);
        params.append('serviceId', serviceId);
        params.append('maxRows', '' + maxRows);

        return this.httpGet('api/exchangeAudit/getExchangeList', { search : params});
    }

    getExchangeEvents(exchangeId:string) : Observable<ExchangeEvent[]> {
        var params = new URLSearchParams();
        params.append('exchangeId', exchangeId);

        return this.httpGet('api/exchangeAudit/getExchangeEvents', { search : params});
    }

    postToExchange(exchangeId: string, exchangeName: string):Observable<any> {
        var request = {
            'exchangeId': exchangeId,
            'exchangeName': exchangeName
        };
        return this.httpPost('api/exchangeAudit/postToExchange', request);
    }

    /*
    getTransformErrorSummaries():Observable<ExchangeAuditTransformErrorSummary[]> {
        return this.httpGet('api/transformErrors/getErrorSummaries');
    }

    getTransformErrorDetail(serviceId:string, systemId:string, exchangeId:string):Observable<ExchangeAuditTransformErrorDetail> {
        var params = new URLSearchParams();
        params.append('serviceId', serviceId);
        params.append('systemId', systemId);
        params.append('exchangeId', exchangeId);

        return this.httpGet('api/transformErrors/getErrorDetails', { search : params});
    }

    rerunFirst(serviceId: string, systemId: string):Observable<ExchangeAuditTransformErrorSummary> {
        var request = {
            'serviceId': serviceId,
            'systemId': systemId
        };
        return this.httpPost('api/transformErrors/rerunFirstExchange', { body : request });
    }

    rerunAll(serviceId: string, systemId: string) : Observable<any> {
        var request = {
            'serviceId': serviceId,
            'systemId': systemId
        };
        return this.httpPost('api/transformErrors/rerunAllExchanges', { body : request });
    }
*/
}
