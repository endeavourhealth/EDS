/// <reference path="../../typings/tsd.d.ts" />

module app.core {
	import RabbitNode = app.models.RabbitNode;
	import RabbitQueue = app.models.RabbitQueue;
	import RabbitExchange = app.models.RabbitExchange;
	import RabbitBinding = app.models.RabbitBinding;
	'use strict';

	export interface IRabbitService {
		getRabbitNodes() : ng.IPromise<RabbitNode[]>;
		pingRabbitNode(address:string) : ng.IPromise<RabbitNode>;
		getRabbitQueues(address:string) : ng.IPromise<RabbitQueue[]>;
		getRabbitExchanges(address:string) : ng.IPromise<RabbitExchange[]>;
		getRabbitBindings(address:string) : ng.IPromise<RabbitBinding[]>;
	}

	export class RabbitService extends BaseHttpService implements IRabbitService {

		getRabbitNodes() : ng.IPromise<RabbitNode[]> {
			return this.httpGet('api/dashboard/rabbitNodes');
		}

		pingRabbitNode(address:string) : ng.IPromise<RabbitNode> {
			var request = {
				params: {
					'address': address
				}
			};
			return this.httpGet('api/dashboard/rabbitNode/ping', request);
		}

		getRabbitQueues(address:string) : ng.IPromise<RabbitQueue[]> {
			var request = {
				params: {
					'address': address
				}
			};
			return this.httpGet('api/dashboard/rabbitNode/queues', request);
		}

		getRabbitExchanges(address:string) : ng.IPromise<RabbitExchange[]> {
			var request = {
				params: {
					'address': address
				}
			};
			return this.httpGet('api/dashboard/rabbitNode/exchanges', request);
		}

		getRabbitBindings(address:string) : ng.IPromise<RabbitBinding[]> {
			var request = {
				params: {
					'address': address
				}
			};
			return this.httpGet('api/dashboard/rabbitNode/bindings', request);
		}
	}

	angular
		.module('app.core')
		.service('RabbitService', RabbitService);
}