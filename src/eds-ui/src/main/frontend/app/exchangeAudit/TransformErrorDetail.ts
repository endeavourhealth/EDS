export class TransformErrorDetail {
    exchangeId: string;
    version: string;
    eventDesc: string;
    transformStart: number;
    transformEnd: number;
    numberBatchIdsCreated: number;
    hadErrors: boolean;
    resubmitted: boolean;
    deleted: number;
    lines: string[];

}
