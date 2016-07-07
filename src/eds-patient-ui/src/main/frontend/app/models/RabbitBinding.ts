module app.models {
    'use strict';

    export class RabbitBinding {
        source: string;
        destination: string;
        routing_key: string;
    }
}