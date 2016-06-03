/// <reference path="../../typings/tsd.d.ts" />

module app.core {
	import ReportActivityItem = app.models.ReportActivityItem;
	import EngineState = app.models.EngineState;
	import FolderItem = app.models.FolderItem;
	import EngineHistoryItem = app.models.EngineHistoryItem;
	'use strict';

	export interface IDashboardService {
		getEngineHistory() : ng.IPromise<EngineHistoryItem[]>;
		getRecentDocumentsData() : ng.IPromise<FolderItem[]>;
		getEngineState() : ng.IPromise<EngineState>;
		getReportActivityData() : ng.IPromise<ReportActivityItem[]>;
		startEngine() : ng.IPromise<any>;
		stopEngine() : ng.IPromise<any>;
	}

	export class DashboardService extends BaseHttpService implements IDashboardService {

		getEngineHistory():ng.IPromise<EngineHistoryItem[]> {
			var request = {
				params: {
					'count': 5
				}
			};

			return this.httpGet('api/dashboard/getEngineHistory', request);
		}

		getRecentDocumentsData():ng.IPromise<FolderItem[]> {
			var request = {
				params: {
					'count': 5
				}
			};

			return this.httpGet('api/dashboard/getRecentDocuments', request);
		}

		getEngineState():ng.IPromise<EngineState> {
			return this.httpGet('api/dashboard/getProcessorStatus');
		}

		getReportActivityData():ng.IPromise<ReportActivityItem[]> {
			var request = {
				params: {
					'count': 5
				}
			};

			return this.httpGet('api/dashboard/getReportActivity', request);
		}

		startEngine() : ng.IPromise<any> {
			return this.httpPost('api/dashboard/startProcessor');
		}

		stopEngine() : ng.IPromise<any> {
			return this.httpPost('api/dashboard/stopProcessor');
		}

	}

	angular
		.module('app.core')
		.service('DashboardService', DashboardService);
}