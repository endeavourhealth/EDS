import {Injectable} from "@angular/core";
import {System} from "./models/System";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "eds-common-js";
import {ItemSummaryList} from "eds-common-js/dist/library/models/ItemSummaryList";
import {FolderItem} from "eds-common-js/dist/folder/models/FolderItem";
import {Http, URLSearchParams} from "@angular/http";

@Injectable()
export class SystemService extends BaseHttp2Service {
	constructor(http : Http) { super(http); }

	getSystems(): Observable<System[]> {
		return this.httpGet('api/library/getSystems');
	}

	getSystemList(): Observable<FolderItem[]> {
		return this.httpGet('api/library/getSystemList');
	}

	deleteSystem(uuid: string): Observable<string> {
		let params = new URLSearchParams();
		params.set('uuid', uuid);
		return this.httpDelete('api/library/deleteSystem', { search : params });
	}
}