import {BaseHttpService} from "./baseHttp.service";
import {RabbitBinding} from "../models/RabbitBinding";
import {RabbitExchange} from "../models/RabbitExchange";
import {RabbitQueue} from "../models/RabbitQueue";
import {RabbitNode} from "../models/RabbitNode";

export interface IRabbitService {
	getRabbitNodes() : ng.IPromise<RabbitNode[]>;
	pingRabbitNode(address:string) : ng.IPromise<RabbitNode>;
	getRabbitQueues(address:string) : ng.IPromise<RabbitQueue[]>;
	getRabbitExchanges(address:string) : ng.IPromise<RabbitExchange[]>;
	getRabbitBindings(address:string) : ng.IPromise<RabbitBinding[]>;
	synchronize(address:string) : any;
}

export class RabbitService extends BaseHttpService implements IRabbitService {

	getRabbitNodes() : ng.IPromise<RabbitNode[]> {
		return this.httpGet('api/rabbit/nodes');
	}

	pingRabbitNode(address:string) : ng.IPromise<RabbitNode> {
		var request = {
			params: {
				'address': address
			}
		};
		return this.httpGet('api/rabbit/ping', request);
	}

	getRabbitQueues(address:string) : ng.IPromise<RabbitQueue[]> {
		var request = {
			params: {
				'address': address
			}
		};
		return this.httpGet('api/rabbit/queues', request);
	}

	getRabbitExchanges(address:string) : ng.IPromise<RabbitExchange[]> {
		var request = {
			params: {
				'address': address
			}
		};
		return this.httpGet('api/rabbit/exchanges', request);
	}

	getRabbitBindings(address:string) : ng.IPromise<RabbitBinding[]> {
		var request = {
			params: {
				'address': address
			}
		};
		return this.httpGet('api/rabbit/bindings', request);
	}

	synchronize(address:string) : any {
		return this.httpPost('api/rabbit/synchronize', address);
	}
}
