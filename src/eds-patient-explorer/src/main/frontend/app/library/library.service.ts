import {Injectable} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";

import {BaseHttp2Service} from "../core/baseHttp2.service";

import {LibraryItem} from "./models/LibraryItem";
import {System} from "../systems/models/System";
import {EntityMap} from "../entities/models/EntityMap";
import {Cohort} from "../cohorts/models/Cohort";
import {DataSet} from "../dataSets/models/Dataset";

@Injectable()
export class LibraryService extends BaseHttp2Service {
	constructor(http: Http) {
		super(http);
	}

	getLibraryItem(uuid: string): Observable<LibraryItem> {
		let params = new URLSearchParams();
		params.set('uuid', uuid);
		return this.httpGet('api/library/getLibraryItem', {search: params});
	}

	saveLibraryItem(libraryItem: LibraryItem): Observable<LibraryItem> {
		return this.httpPost('api/library/saveLibraryItem', libraryItem);
	}

	deleteLibraryItem(uuid: string): Observable<any> {
		var libraryItem = {uuid: uuid};
		return this.httpPost('api/library/deleteLibraryItem', libraryItem);
	}

	getEntityMap(): Observable<EntityMap> {
		return this.httpGet('api/entity/getEntityMap');
	}

	getSystems(): Observable<System[]> {
		return this.httpGet('api/library/getSystems');
	}

	getCohorts(): Observable<Cohort[]> {
		return this.httpGet('api/library/getQueries');
	}

	getDatasets(): Observable<DataSet[]> {
		return this.httpGet('api/library/getDataSets');
	}

	getProtocols(serviceId: string): Observable<LibraryItem[]> {
		let params = new URLSearchParams();
		params.set('serviceId', serviceId);

		return this.httpGet('api/library/getProtocols', {search: params});
	}
}
