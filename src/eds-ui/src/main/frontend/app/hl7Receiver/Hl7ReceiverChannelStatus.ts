
import {Hl7ReceiverTransformEvent} from "./Hl7ReceiverTransformEvent";
export class Hl7ReceiverChannelStatus {

    id: number;
    name: string;
    paused: boolean;
    lastMessageId: number;
    lastMessageReceived: number;
    errorMessageId: number;
    errorMessageReceived: number;
    errorMessageType: string;
    errorMessage: string;
    transformQueueSize: number;
    transformQueueFirstMessageId: number;
    transformQueueFirstMessageDate: number;
    transformQueueFirstMessageType: string;
    transformQueueFirstStatus: Hl7ReceiverTransformEvent[];
}
