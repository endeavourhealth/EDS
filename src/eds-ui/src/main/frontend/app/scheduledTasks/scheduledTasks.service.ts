import {BaseHttp2Service} from "eds-common-js";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {ScheduledTaskAudit} from "./models/ScheduledTaskAudit";


@Injectable()
export class ScheduledTasksService extends BaseHttp2Service {


    constructor(http : Http) {
        super (http);
    }

    getScheduledTaskSummary() : Observable<ScheduledTaskAudit[]> {

        var params = new URLSearchParams();

        return this.httpGet('api/scheduledTask/summary', { search: params });
    }

    getScheduledTaskHistory(applicationName: string, taskName: string) : Observable<ScheduledTaskAudit[]> {

        var params = new URLSearchParams();
        params.append('applicationName', applicationName);
        params.append('taskName', taskName);

        return this.httpGet('api/scheduledTask/history', { search: params });
    }

}
