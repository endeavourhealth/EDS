import {Service} from "../services/models/Service";
export class TransformErrorSummary {
    service: Service;
    systemId: string;
    systemName: string;
    countExchanges: number;
    exchangeIds: string[];
}
