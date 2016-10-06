import {BaseHttpService} from "./baseHttp.service";
import {Service} from "../models/Service";
import {StorageStatistics} from "../models/StorageStatistics";

export interface IStatsService {
	getStorageStatistics(services : Service[]):ng.IPromise<StorageStatistics[]>;
}

export class StatsService extends BaseHttpService implements IStatsService {

	getStorageStatistics(services : Service[]):ng.IPromise<StorageStatistics[]> {
		var serviceList = new Array();
		var systemList = new Array();
		for (var i = 0; i < services.length; ++i) {
			var serviceId = services[i].uuid;
			var systemId = services[i].endpoints[0].systemUuid; //TODO: pick first system registered against system for now - later offer choice
			serviceList.push(serviceId);
			systemList.push(systemId);
		}

		var request = {
			params: {
				'serviceList': serviceList,
				'systemList': systemList
			}
		};

		return this.httpGet('api/stats/getStorageStatistics', request);
	}
}
