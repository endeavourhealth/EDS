import {Injectable} from "@angular/core";
import {Http, RequestOptions} from "@angular/http";
import {BaseHttp2Service} from "../core/baseHttp2.service";

@Injectable()
export class SqlEditorService extends BaseHttp2Service {
	constructor(http: Http) {
		super(http);
	}

	getTableData() {
		return this.httpGet('api/sqlEditor/getTableData');
	}

	runQuery(sql : string) {
		return this.httpPost('api/sqlEditor/runQuery', sql);
	}
}
