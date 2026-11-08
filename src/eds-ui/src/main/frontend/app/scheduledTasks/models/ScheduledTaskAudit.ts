import {ScheduledTaskRule} from "./ScheduledTaskRule";
export class ScheduledTaskAudit {

    //properties provided by the server
    applicationName: string;
    taskName: string;
    taskParameters: string;
    timestamp: number;
    hostName: string;
    success: boolean;
    errorMessage: string;

    //client cached variables
    rule: ScheduledTaskRule;
    notRunWarning: boolean;
}
