import {BaseHttpService} from "./baseHttp.service";
import {TransformErrorSummary} from "../models/TransformErrorSummary";
import {TransformErrorDetail} from "../models/TransformErrorDetail";

export interface ITransformErrorsService {

    getTransformErrorSummaries():ng.IPromise<TransformErrorSummary[]>;
    getTransformErrorDetail(serviceId: string, systemId: string, exchangeId: string):ng.IPromise<TransformErrorDetail>;
}

export class TransformErrorsService extends BaseHttpService implements ITransformErrorsService {


    getTransformErrorSummaries():angular.IPromise<TransformErrorSummary[]> {
        return this.httpGet('api/transformAudit/getErrors');
    }

    getTransformErrorDetail(serviceId:string, systemId:string, exchangeId:string):angular.IPromise<TransformErrorDetail> {
        var request = {
            params: {
                'serviceId': serviceId,
                'systemId': systemId,
                'exchangeId': exchangeId
            }
        };

        return this.httpGet('api/transformAudit/getErrorDetails', request);
    }

   /* reQueueFirstTransform(serviceId:string, systemId:string):angular.IPromise<TransformErrorSummary> {

    }

    reQueueAllTransforms(serviceId:string, systemId:string):angular.IPromise<TransformErrorSummary> {

    }*/
}
