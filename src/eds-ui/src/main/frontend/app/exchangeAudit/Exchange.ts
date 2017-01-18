import {ExchangeEvent} from "./ExchangeEvent";
import {TransformErrorDetail} from "./TransformErrorDetail";

export class Exchange {
    exchangeId: string;
    timestamp: number;
    headers: {};
    events: ExchangeEvent[];
    transformAudits: TransformErrorDetail[];
}
