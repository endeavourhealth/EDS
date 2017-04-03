import {BaseHttp2Service} from "../core/baseHttp2.service";
import {Service} from "../services/models/Service";
import {StorageStatistics} from "./StorageStatistics";
import {Injectable} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";

@Injectable()
export class StatsService extends BaseHttp2Service {
	constructor (http : Http) { super (http); }

	getStorageStatistics(services : Service[]):Observable<StorageStatistics[]> {
		let params = new URLSearchParams();

		for (var i = 0; i < services.length; ++i) {
			var serviceId = services[i].uuid;
			var systemId = services[i].endpoints[0].systemUuid; //TODO: pick first system registered against system for now - later offer choice
			params.append('serviceList', serviceId);
			params.append('systemList', systemId);
		}

		return this.httpGet('api/stats/getStorageStatistics', { search : params });
	}
}
