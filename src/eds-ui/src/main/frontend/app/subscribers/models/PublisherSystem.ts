export class PublisherSystem {

    uuid: string; //system UUID
    name: string; //Emis, TPP, etc
    publisherMode: string; //regular, bulk, auto-fail, draft

    //data receipt into DDS
    lastReceivedTimestamp: number;
    lastReceivedExtractDate: number;
    lastReceivedExtractCutoff: number;

    //INBOUND data processing
    processingInError: boolean;
    processingInErrorMessage: string;
    lastProcessedInTimestamp: number;
    lastProcessedInExtractDate: number;
    lastProcessedInExtractCutoff: number;

    //OUTBOUND data processing
    lastProcessedOutTimestamp: number;
    lastProcessedOutExtractDate: number;
    lastProcessedOutExtractCutoff: number;

    //OUTBOUND data writing
    lastWriteOutTimestamp: number;
    lastWriteOutExtractDate: number;
    lastWriteOutExtractCutoff: number;
    writeOutError: boolean;
    writeOutErrorMessage: string;

    //cached things from client
    inboundBehindDays: number; //number of days behind inbound processing is
    inboundBehindWarning: string; //text message to go with the above
    outboundBehindDays: number;
    outboundBehindWarning: string;
    outboundWriteBehindDays: number;
    outboundWriteBehindWarning: string;
}