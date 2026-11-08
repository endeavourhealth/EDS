export class Hl7v2ApiStatus {

    odsCode: string;
    name: string;
    lastReceivedTimestamp: number;
    lastReceivedMessageId: string;
    lastReceivedId: number;
    queueSize: number;
    queueOldestTimestamp: number;
    queueMessageId: string;
    queueId: number;

}