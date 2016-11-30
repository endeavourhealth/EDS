export class RabbitRate {
    rate : number;
}

export class RabbitMessageStats {
    publish_details : RabbitRate;
    publish_in_details : RabbitRate;
    publish_out_details : RabbitRate;
}
