import {RabbitMessageStats} from "./RabbitMessageStats";

export class RabbitQueue {
    name: string;
    messages_ready: number;
    message_stats : RabbitMessageStats;
}
