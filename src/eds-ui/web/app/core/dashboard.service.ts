/// <reference path="../../typings/tsd.d.ts" />

module app.core {
	import FolderItem = app.models.FolderItem;
	import RabbitNode = app.models.RabbitNode;
	import RabbitQueue = app.models.RabbitQueue;
	'use strict';

	export interface IDashboardService {
		getRecentDocumentsData() : ng.IPromise<FolderItem[]>;
		getRabbitNodes() : ng.IPromise<RabbitNode[]>;
		pingRabbitNode(node:RabbitNode) : ng.IPromise<RabbitNode>;
		getRabbitQueues(node:RabbitNode) : ng.IPromise<RabbitQueue[]>;
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

		getRabbitNodes() : ng.IPromise<RabbitNode[]> {
			return this.httpGet('api/dashboard/rabbitNodes');
		}

		pingRabbitNode(node:RabbitNode) : ng.IPromise<RabbitNode> {
			var request = {
				params: {
					'address': node.address
				}
			};
			return this.httpGet('api/dashboard/rabbitNode/ping', request);
		}

		getRabbitQueues(node:RabbitNode) : ng.IPromise<RabbitQueue[]> {
			var request = {
				params: {
					'address': node.address
				}
			};
			return this.httpGet('api/dashboard/rabbitNode/queues', request);
		}
	}

	angular
		.module('app.core')
		.service('DashboardService', DashboardService);
}