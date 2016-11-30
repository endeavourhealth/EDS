import {Injectable} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";

import {BaseHttp2Service} from "../core/baseHttp2.service";
import {FolderItem} from "../library/models/FolderContent";

@Injectable()
export class DashboardService extends BaseHttp2Service {
	constructor(http : Http) { super (http); }

	getRecentDocumentsData():Observable<FolderItem[]> {
		let params = new URLSearchParams();
		params.set('count','5');

		return this.httpGet('api/dashboard/getRecentDocuments', {search : params});
	}
}
