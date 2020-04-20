export class TransformErrorDetail {
    exchangeId: string;
    version: string;
    eventDesc: string;
    transformStart: number;
    transformEnd: number;
    numberBatchIdsCreated: number;
    transformError: boolean;
    transformSuccess: boolean;
    transformInProgress: boolean;
    resubmitted: boolean;
    deleted: number;
    lines: string[];

}
