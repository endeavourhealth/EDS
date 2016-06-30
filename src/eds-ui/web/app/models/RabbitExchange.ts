module app.models {
    'use strict';

    export class RabbitRate {
        rate : number;
    }

    export class RabbitMessageStats {
        publish_in_details : RabbitRate;
        publish_out_details : RabbitRate;
    }

    export class RabbitExchange {
        name: string;
        message_stats: RabbitMessageStats;
    }
}