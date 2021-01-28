import {BaseHttp2Service} from "eds-common-js";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {Exchange} from "./Exchange";
import {TransformErrorSummary} from "./TransformErrorSummary";
import {TransformErrorDetail} from "./TransformErrorDetail";
import {Protocol} from "./Protocol";

@Injectable()
export class ExchangeAuditService extends BaseHttp2Service {

    //search filters
    searchMode: string;
    exchangesToShow: number;
    exchangeIdSearch: string;
    exchangeSearchFrom: Date;
    exchangeSearchTo: Date;

    //moved from the component so we don't lose their values
    postFilterFileTypesSelected: string;
    postReason: string;
    postMode: string;
    postExchange: string;
    postFilterFileTypes: boolean;
    postDeleteErrorState: boolean;
    postShowAllSubscriberConfigNames: boolean;

    constructor(http:Http) {
        super(http);

        var vm = this;
        vm.exchangesToShow = 20;
        vm.searchMode = 'Recent';
        vm.postMode = 'Onwards';
        vm.postShowAllSubscriberConfigNames = false;
    }


    getExchangesByDate(serviceId: string, systemId: string, maxRows: number, dateFrom: Date, dateTo: Date) : Observable<Exchange[]> {
        console.log('Getting for service ' + serviceId + ' system ' + systemId + ' and ' + maxRows + ' from ' + dateFrom + ' to ' + dateTo);

        var params = new URLSearchParams();
        params.append('serviceId', serviceId);
        params.append('systemId', systemId);
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

        return this.httpGet('api/exchangeAudit/getExchangesByDate', { search : params});
    }

    getRecentExchanges(serviceId: string, systemId: string, maxRows: number) : Observable<Exchange[]> {
        console.log('Getting for service ' + serviceId + ' system ' + systemId + ' and ' + maxRows);

        var params = new URLSearchParams();
        params.append('serviceId', serviceId);
        params.append('systemId', systemId);
        params.append('maxRows', '' + maxRows);

        return this.httpGet('api/exchangeAudit/getRecentExchanges', { search : params});
    }

    getExchangesFromFirstError(serviceId: string, systemId: string, maxRows: number) : Observable<Exchange[]> {
        console.log('Getting for service ' + serviceId + ' system ' + systemId + ' and ' + maxRows);

        var params = new URLSearchParams();
        params.append('serviceId', serviceId);
        params.append('systemId', systemId);
        params.append('maxRows', '' + maxRows);

        return this.httpGet('api/exchangeAudit/getExchangesFromFirstError', { search : params});
    }

    getExchangeById(serviceId: string, systemId: string, exchangeId:string) : Observable<Exchange[]> {
        var params = new URLSearchParams();
        console.log('Getting for service id ' + serviceId + ' and exchange id ' + exchangeId);
        params.append('serviceId', serviceId);
        params.append('systemId', systemId);
        params.append('exchangeId', exchangeId);

        return this.httpGet('api/exchangeAudit/getExchangeById', { search : params});
    }

    /*getExchangeEvents(exchangeId:string) : Observable<ExchangeEvent[]> {
        var params = new URLSearchParams();
        params.append('exchangeId', exchangeId);

        return this.httpGet('api/exchangeAudit/getExchangeEvents', { search : params});
    }*/

    postToExchange(exchangeId: string, serviceId: string, systemId: string, exchangeName: string, postMode: string,
                   specificSubscriberConfigNames: string[], fileTypesToFilterOn: string, deleteErrorState: boolean,
                    reason: string) : Observable<string> {

        var request = {
            'exchangeId': exchangeId,
            'serviceId': serviceId,
            'systemId': systemId,
            'exchangeName': exchangeName,
            'postMode': postMode,
            'specificSubscriberConfigNames': specificSubscriberConfigNames,
            'fileTypesToFilterOn': fileTypesToFilterOn,
            'deleteTransformErrorState': deleteErrorState,
            'reason': reason
        };
        return this.httpPost('api/exchangeAudit/postToExchange', request);
    }



    getTransformErrorSummaries():Observable<TransformErrorSummary[]> {
        return this.httpGet('api/exchangeAudit/getTransformErrorSummaries');
    }

    getInboundTransformAudits(serviceId:string, systemId:string, exchangeId:string,
                              getAllAuditsAndEvents:boolean) : Observable<TransformErrorDetail[]> {
        var params = new URLSearchParams();
        params.append('serviceId', serviceId);
        params.append('systemId', systemId);
        params.append('exchangeId', exchangeId);
        params.append('getAllAuditsAndEvents', '' + getAllAuditsAndEvents);

        return this.httpGet('api/exchangeAudit/getInboundTransformAudits', { search : params});
    }

    /*rerunFirstExchangeInError(serviceId: string, systemId: string):Observable<TransformErrorSummary> {
        var request = {
            'serviceId': serviceId,
            'systemId': systemId
        };
        return this.httpPost('api/exchangeAudit/rerunFirstExchangeInError', request);
    }*/


    getTransformErrorLines(serviceId:string, systemId:string, exchangeId:string, version:string) : Observable<string[]> {
        var params = new URLSearchParams();
        params.append('serviceId', serviceId);
        params.append('systemId', systemId);
        params.append('exchangeId', exchangeId);
        params.append('version', version);

        return this.httpGet('api/exchangeAudit/getTransformErrorLines', { search : params});
    }

    /*getProtocolsList(serviceId:string, onlySubscriberProtocols:boolean) : Observable<Protocol[]> {

        var params = new URLSearchParams();
        params.append('serviceId', serviceId);
        params.append('onlySubscriberProtocols', '' + onlySubscriberProtocols);

        return this.httpGet('api/exchangeAudit/getProtocolsForService', { search : params});
    }*/

    getSubscriberConfigNameList(serviceId:string) : Observable<string[]> {

        var params = new URLSearchParams();
        params.append('serviceId', serviceId);

        return this.httpGet('api/exchangeAudit/getSubscriberConfigNamesForService', { search : params});
    }

    getAllSubscriberConfigNameList() : Observable<string[]> {

        var params = new URLSearchParams();
        return this.httpGet('api/exchangeAudit/getAllSubscriberConfigNames', { search : params});
    }

    rerunAllExchangesInError(serviceId: string, systemId: string) : Observable<any> {
        var request = {
            'serviceId': serviceId,
            'systemId': systemId
        };
        return this.httpPost('api/exchangeAudit/rerunAllExchangesInError', request);
    }


    rerunAllExchangesInErrorForServices(summaries: TransformErrorSummary[]) : Observable<any> {
        var request = [];
        var arrayLength = summaries.length;
        for (var i = 0; i < arrayLength; i++) {
            var summary = summaries[i];
            request.push({
                'serviceId': summary.service.uuid,
                'systemId': summary.systemId
            });
        }

        return this.httpPost('api/exchangeAudit/requeueServicesInError', request);
    }

    addExchangeEvent(exchangeId: string, text: string) : Observable<any> {
        var request = {
            'exchangeId': exchangeId,
            'text': text
        };
        return this.httpPost('api/exchangeAudit/addExchangeEvent', request);
    }
}
