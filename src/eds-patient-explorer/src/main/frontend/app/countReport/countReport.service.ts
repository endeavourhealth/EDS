import {Injectable} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {LibraryItem} from "../library/models/LibraryItem";
import {ReportParams} from "./models/ReportParams";

@Injectable()
export class CountReportService extends BaseHttp2Service {
	constructor(http: Http) {
		super(http);
	}

	runReport(uuid: string, reportParams: Map<string, string>): Observable<LibraryItem> {
		let params = new URLSearchParams();
		params.set('uuid', uuid);

		return this.httpPost('api/countReport/runReport', reportParams, {search: params});
	}

	exportNHSNumbers(uuid: string): Observable<string> {
		let params = new URLSearchParams();
		params.set('uuid', uuid);
		return this.httpGet('api/countReport/exportNHS', {search: params});
	}

	exportData(uuid: string): Observable<string> {
		let params = new URLSearchParams();
		params.set('uuid', uuid);
		return this.httpGet('api/countReport/exportData', {search: params});
	}

}
