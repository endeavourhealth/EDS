module app.models {
    'use strict';

    export class RabbitExchange {
        name: string;

        message_stats: RabbitMessageStats;
    }
}