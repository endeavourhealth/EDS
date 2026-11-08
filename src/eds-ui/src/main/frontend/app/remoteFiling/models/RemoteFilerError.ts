export class RemoteFilerError {
    serviceName: string;
    serviceOdsCode: string;
    serviceId: string;
    systemId: string;
    exchangeId: string;
    batchId: string;
    queuedMessageId: string;
    dtFileSent: number;
    dtFileFilingAttempted: number;
    fileFilingSuccess: boolean;
    filingFailureMessage: string;
    dtInserted: number;
}