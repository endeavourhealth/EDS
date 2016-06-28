/// <reference path="../../typings/index.d.ts" />

module app.core {
	import FolderItem = app.models.FolderItem;
	'use strict';

	export interface IDashboardService {
		getRecentDocumentsData() : ng.IPromise<FolderItem[]>;
	}

	export class DashboardService extends BaseHttpService implements IDashboardService {

		getRecentDocumentsData():ng.IPromise<FolderItem[]> {
			var request = {
				params: {
					'count': 5
				}
			};

			return this.httpGet('api/dashboard/getRecentDocuments', request);
		}

	}

	angular
		.module('app.core')
		.service('DashboardService', DashboardService);
}