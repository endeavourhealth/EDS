/// <reference path="../../typings/index.d.ts" />

module app.core {
	import StorageStatistics = app.models.StorageStatistics;
	import Service = app.models.Service;

	'use strict';

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

	angular
		.module('app.core')
		.service('StatsService', StatsService);
}