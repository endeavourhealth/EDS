export class SubscriberZipFileUUID {
    subscriberId: number;
    queuedMessageUUID: string; //queue
    queuedMessageBody: string;
    filingOrder: number;
    fileSent: string;
    fileFilingAttempted: string;
    fileFilingSuccess: boolean;
    filingFailureMessage: string;

}
