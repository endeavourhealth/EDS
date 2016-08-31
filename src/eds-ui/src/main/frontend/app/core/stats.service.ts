/// <reference path="../../typings/index.d.ts" />

module app.core {
	import StatsPatient = app.models.StatsPatient;
	import StatsEvent = app.models.StatsEvent;
	'use strict';

	export interface IStatsService {
		getStatsPatients(serviceId : string):ng.IPromise<StatsPatient[]>;
		getStatsEvents(serviceId : string):ng.IPromise<StatsEvent[]>;
	}

	export class StatsService extends BaseHttpService implements IStatsService {

		getStatsPatients(serviceId : string):ng.IPromise<StatsPatient[]> {
			var request = {
				params: {
					'serviceId': serviceId
				}
			};

			return this.httpGet('api/stats/getStatsPatients', request);
		}

		getStatsEvents(serviceId : string):ng.IPromise<StatsEvent[]> {
			var request = {
				params: {
					'serviceId': serviceId
				}
			};

			return this.httpGet('api/stats/getStatsEvents', request);
		}
	}

	angular
		.module('app.core')
		.service('StatsService', StatsService);
}