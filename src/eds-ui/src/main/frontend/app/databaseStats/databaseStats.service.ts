import {BaseHttp2Service} from "eds-common-js";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {DatabaseHost} from "./DatabaseHost";
import {Database} from "./Database";

@Injectable()
export class DatabaseStatsService extends BaseHttp2Service {

    constructor(http : Http) {
        super (http);
    }

    getDatabaseServers() : Observable<DatabaseHost[]> {
        //console.log('Getting database servers');
        return this.httpGet('api/databaseStats/databaseServers', {});
    }

    getDatabaseSizes(host: DatabaseHost) : Observable<Database[]> {
        //console.log('Getting database servers');
        return this.httpGet('api/databaseStats/databases/' + host.type + '/' + host.host, {});
    }
}
