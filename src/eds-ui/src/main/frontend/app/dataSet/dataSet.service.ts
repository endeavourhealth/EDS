import {Injectable} from "@angular/core";
import {Http} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "eds-common-js";
import {DataSet} from "./models/Dataset";
import {EntityMap} from "./models/EntityMap";
@Injectable()
export class DataSetService extends BaseHttp2Service {
	constructor(http : Http) { super(http); }

	getDatasets(): Observable<DataSet[]> {
		return this.httpGet('api/library/getDataSets');
	}

	getEntityMap(): Observable<EntityMap> {
		return this.httpGet('api/entity/getEntityMap');
	}
}