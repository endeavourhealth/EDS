import {ScheduledTaskRule} from "./ScheduledTaskRule";
export class ScheduledTaskAudit {
    applicationName: string;
    taskName: string;
    taskParameters: string;
    timestamp: number;
    hostName: string;
    success: boolean;
    errorMessage: string;
    rule: ScheduledTaskRule;

}
