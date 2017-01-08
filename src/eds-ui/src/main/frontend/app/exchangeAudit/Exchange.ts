import {ExchangeEvent} from "./ExchangeEvent";

export class Exchange {
    exchangeId: string;
    timestamp: number;
    headers: {};
    events: ExchangeEvent[];
}
