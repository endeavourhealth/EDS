export class SystemStatus {

    systemName: string;
    lastDataDate: number;
    lastDataReceived: number;
    processingUpToDate: boolean;
    processingInError: boolean;
    lastDateSuccessfullyProcessed: number;
    cachedLastDataDateDesc: string;

    constructor() {}
}
