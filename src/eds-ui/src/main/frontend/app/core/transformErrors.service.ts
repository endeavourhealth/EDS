import {BaseHttpService} from "./baseHttp.service";
import {TransformErrorSummary} from "../models/TransformErrorSummary";
import {TransformErrorDetail} from "../models/TransformErrorDetail";

export interface ITransformErrorsService {

    getTransformErrorSummaries():ng.IPromise<TransformErrorSummary[]>;
    getTransformErrorDetail(serviceId: string, systemId: string, exchangeId: string):ng.IPromise<TransformErrorDetail>;
    rerunFirst(serviceId: string, systemId: string):ng.IPromise<TransformErrorSummary>;
    rerunAll(serviceId: string, systemId: string);
}

export class TransformErrorsService extends BaseHttpService implements ITransformErrorsService {


    getTransformErrorSummaries():angular.IPromise<TransformErrorSummary[]> {
        return this.httpGet('api/transformErrors/getErrorSummaries');
    }

    getTransformErrorDetail(serviceId:string, systemId:string, exchangeId:string):angular.IPromise<TransformErrorDetail> {
        var request = {
            params: {
                'serviceId': serviceId,
                'systemId': systemId,
                'exchangeId': exchangeId
            }
        };

        return this.httpGet('api/transformErrors/getErrorDetails', request);
    }

    rerunFirst(serviceId: string, systemId: string):angular.IPromise<TransformErrorSummary> {
        /*var request = {
            params: {
                'serviceId': serviceId,
                'systemId': systemId
            }
        };*/
        var request = {
            'serviceId': serviceId,
            'systemId': systemId
        };
        return this.httpPost('api/transformErrors/rerunFirstExchange', request);
    }

    rerunAll(serviceId: string, systemId: string) {
        /*var request = {
            params: {
                'serviceId': serviceId,
                'systemId': systemId
            }
        };*/
        var request = {
            'serviceId': serviceId,
            'systemId': systemId
        };
        this.httpPost('api/transformErrors/rerunAllExchanges', request);
    }

}
