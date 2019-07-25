import {BaseHttp2Service} from "eds-common-js";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {FrailtyStat} from "./FrailtyStat";

@Injectable()
export class FrailtyApiService extends BaseHttp2Service {

    constructor(http : Http) {
        super (http);
    }

    getRecentStats(minutesBack: number, groupBy: string) : Observable<FrailtyStat[]> {

        var params = new URLSearchParams();
        params.append('minutesBack', '' + minutesBack);
        params.append('groupBy', groupBy);

        return this.httpGet('api/frailtyApi/recentStats', { search: params });
    }

    downloadMonthlyStats(): Observable<string> {
        return this.httpGet('api/frailtyApi/downloadMonthlyStats');
    }
}
