module app.models {
    'use strict';

    export class RabbitQueue {
        name: string;
        messages: number;
        messages_ready: number;
        messages_unacknowledged: number;
    }
}