export class SystemStatus {

    systemName: string;
    lastDataDate: number;
    lastDataReceived: number;
    processingUpToDate: boolean;
    processingInError: boolean;
    lastDateSuccessfullyProcessed: number;
    lastDataDateSuccessfullyProcessed: number;
    publisherMode: string;
    cachedLastDataDateDesc: string;
    lastDataDateSuccessfullyProcessedDesc: string;

    constructor() {}
}
