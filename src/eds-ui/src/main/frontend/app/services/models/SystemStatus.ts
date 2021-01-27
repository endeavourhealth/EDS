export class SystemStatus {

    //returned from endpoint
    systemName: string;
    lastReceivedExtract: number;
    lastReceivedExtractDate: number;
    lastReceivedExtractCutoff: number;
    processingUpToDate: boolean;
    processingInError: boolean;
    lastProcessedExtract: number;
    lastProcessedExtractDate: number;
    lastProcessedExtractCutoff: number;
    publisherMode: string;

    //cached variables
    cachedLastDataDateDesc: string;
    cachedLastProcessedDiffDesc: string;

    constructor() {}
}
