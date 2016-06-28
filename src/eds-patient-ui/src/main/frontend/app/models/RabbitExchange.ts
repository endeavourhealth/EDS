module app.models {
    'use strict';

    export class RabbitRate {
        rate : number;
    }

    export class RabbitMessageStats {
        publish_details : RabbitRate;
        deliver_get_details : RabbitRate;
    }

    export class RabbitExchange {
        name: string;
        message_stats: RabbitMessageStats;
    }
}