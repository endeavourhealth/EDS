import {BaseHttp2Service} from "../core/baseHttp2.service";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {Exchange} from "./Exchange";
import {ExchangeEvent} from "./ExchangeEvent";
import {TransformErrorSummary} from "./TransformErrorSummary";
import {TransformErrorDetail} from "./TransformErrorDetail";

@Injectable()
export class ExchangeAuditService extends BaseHttp2Service {
    constructor(http:Http) { super(http); }


    getExchangeList(serviceId:string, maxRows:number, dateFrom:Date, dateTo:Date) : Observable<Exchange[]> {
        console.log('Getting for service id ' + serviceId + ' and ' + maxRows + ' from ' + dateFrom + ' to ' + dateTo);

        var params = new URLSearchParams();
        params.append('serviceId', serviceId);
        params.append('maxRows', '' + maxRows);
        if (dateFrom) {
            params.append('dateFrom', '' + dateFrom.getTime());
        } else {
            params.append('dateFrom', '');
        }
        if (dateTo) {
            params.append('dateTo', '' + dateTo.getTime());
        } else {
            params.append('dateTo', '');
        }

        return this.httpGet('api/exchangeAudit/getExchangeList', { search : params});
    }

    getExchangeById(serviceId:string, exchangeId:string) : Observable<Exchange[]> {
        var params = new URLSearchParams();
        console.log('Getting for service id ' + serviceId + ' and exchange id ' + exchangeId);
        params.append('serviceId', serviceId);
        params.append('exchangeId', exchangeId);

        return this.httpGet('api/exchangeAudit/getExchangeById', { search : params});
    }

    getExchangeEvents(exchangeId:string) : Observable<ExchangeEvent[]> {
        var params = new URLSearchParams();
        params.append('exchangeId', exchangeId);

        return this.httpGet('api/exchangeAudit/getExchangeEvents', { search : params});
    }

    postToExchange(exchangeId: string, serviceId: string, exchangeName: string, postAllExchanges: boolean):Observable<any> {
        var request = {
            'exchangeId': exchangeId,
            'serviceId': serviceId,
            'exchangeName': exchangeName,
            'postAllExchanges': postAllExchanges
        };
        return this.httpPost('api/exchangeAudit/postToExchange', request);
    }



    getTransformErrorSummaries():Observable<TransformErrorSummary[]> {
        return this.httpGet('api/exchangeAudit/getTransformErrorSummaries');
    }

    getTransformErrorDetail(serviceId:string, systemId:string, exchangeId:string,
                            getMostRecent:boolean, getErrorLines:boolean) : Observable<TransformErrorDetail[]> {
        var params = new URLSearchParams();
        params.append('serviceId', serviceId);
        params.append('systemId', systemId);
        params.append('exchangeId', exchangeId);
        params.append('getMostRecent', '' + getMostRecent);
        params.append('getErrorLines', '' + getErrorLines);

        return this.httpGet('api/exchangeAudit/getTransformErrorDetails', { search : params});
    }

    rerunFirstExchangeInError(serviceId: string, systemId: string):Observable<TransformErrorSummary> {
        var request = {
            'serviceId': serviceId,
            'systemId': systemId
        };
        return this.httpPost('api/exchangeAudit/rerunFirstExchangeInError', request);
    }

    rerunAllExchangesInError(serviceId: string, systemId: string) : Observable<any> {
        var request = {
            'serviceId': serviceId,
            'systemId': systemId
        };
        return this.httpPost('api/exchangeAudit/rerunAllExchangesInError', request);
    }

    getTransformErrorLines(serviceId:string, systemId:string, exchangeId:string, version:string) : Observable<string[]> {
        var params = new URLSearchParams();
        params.append('serviceId', serviceId);
        params.append('systemId', systemId);
        params.append('exchangeId', exchangeId);
        params.append('version', version);

        return this.httpGet('api/exchangeAudit/getTransformErrorLines', { search : params});
    }


}
