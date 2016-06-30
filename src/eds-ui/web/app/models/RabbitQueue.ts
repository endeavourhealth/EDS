module app.models {
    'use strict';

    export class RabbitQueue {
        name: string;
        messages_ready: number;
        message_stats : RabbitMessageStats;
    }
}