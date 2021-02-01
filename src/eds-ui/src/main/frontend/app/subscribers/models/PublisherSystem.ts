export class PublisherSystem {

    uuid: string; //system UUID
    name: string; //Emis, TPP, etc
    publisherMode: string; //regular, bulk, auto-fail, draft

    //data receipt into DDS
    lastReceivedExtract: number;
    lastReceivedExtractDate: number;
    lastReceivedExtractCutoff: number;

    //INBOUND data processing
    processingInError: boolean;
    processingInErrorMessage: string;
    lastProcessedInExtract: number;
    lastProcessedInExtractDate: number;
    lastProcessedInExtractCutoff: number;

    //OUTBOUND data processing
    lastProcessedOutExtract: number;
    lastProcessedOutExtractDate: number;
    lastProcessedOutExtractCutoff: number;

    //cached things from client
    inboundBehindDays: number; //number of days behind inbound processing is
    inboundBehindWarning: string; //text message to go with the above
    outboundBehindDays: number;
    outboundBehindWarning: string;
}