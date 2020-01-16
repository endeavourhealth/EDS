import {TransformErrorDetail} from "./TransformErrorDetail";

export class Exchange {
    exchangeId: string;
    timestamp: number;
    headers: {};
    bodyLines: string[];
    inError: boolean;
    transformAudits: TransformErrorDetail[];
    exchangeSizeDesc: string;
    exchangeSizeBytes: number;
    queueRoutingKeys: {};
}
