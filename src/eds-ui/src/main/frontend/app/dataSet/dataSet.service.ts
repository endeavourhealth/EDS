import {Injectable} from "@angular/core";
import {URLSearchParams, Http} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "eds-common-js";
import {DataSet} from "./models/Dataset";
import {EntityMap} from "./models/EntityMap";
import {Dpa} from "../dpa/models/Dpa";

@Injectable()
export class DataSetService extends BaseHttp2Service {
	constructor(http : Http) { super(http); }

	getDatasets(): Observable<DataSet[]> {
		return this.httpGet('api/library/getDataSets');
	}

	getEntityMap(): Observable<EntityMap> {
		return this.httpGet('api/entity/getEntityMap');
	}


	//**  NEW CODE FROM HERE  **//
	getAllDataSets(): Observable<DataSet[]> {
		return this.httpGet('api/dataSet');
	}

	getDataSet(uuid : string) : Observable<DataSet> {
		let params = new URLSearchParams();
		params.set('uuid',uuid);
		return this.httpGet('api/dataSet', { search : params });
	}

	saveDataSet(dataset : DataSet) : Observable<any> {
		return this.httpPost('api/dataSet', dataset);
	}

	deleteDataSet(uuid : string) : Observable<any> {
		let params = new URLSearchParams();
		params.set('uuid',uuid);
		return this.httpDelete('api/dataSet', { search : params });
	}

	search(searchData : string) : Observable<DataSet[]> {
		let params = new URLSearchParams();
		params.set('searchData',searchData);
		return this.httpGet('api/dataSet', { search : params });
	}

	getLinkedDpas(uuid : string) :  Observable<Dpa[]> {
		let params = new URLSearchParams();
		params.set('uuid',uuid);
		return this.httpGet('api/dataSet/dpas', { search : params });
	}
}