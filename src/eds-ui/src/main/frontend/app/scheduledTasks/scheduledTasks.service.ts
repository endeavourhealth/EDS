import {BaseHttp2Service} from "eds-common-js";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {ScheduledTaskAudit} from "./models/ScheduledTaskAudit";
import {ScheduledTaskRule} from "./models/ScheduledTaskRule";


@Injectable()
export class ScheduledTasksService extends BaseHttp2Service {


    constructor(http : Http) {
        super (http);
    }


    getScheduledTaskRules() : Observable<ScheduledTaskRule[]> {

        var params = new URLSearchParams();

        return this.httpGet('api/scheduledTask/rules', { search: params });
    }

    getScheduledTaskSummary() : Observable<ScheduledTaskAudit[]> {

        var params = new URLSearchParams();

        return this.httpGet('api/scheduledTask/summary', { search: params });
    }

    getScheduledTaskHistory(applicationName: string, taskName: string, dFrom: Date, dTo: Date) : Observable<ScheduledTaskAudit[]> {

        var params = new URLSearchParams();
        params.append('applicationName', applicationName);
        params.append('taskName', taskName);
        params.append('from', '' + dFrom.getTime());
        params.append('to', '' + dTo.getTime());

        return this.httpGet('api/scheduledTask/history', { search: params });
    }


}
