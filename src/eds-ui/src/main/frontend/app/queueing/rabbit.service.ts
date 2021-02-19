import {Injectable} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "eds-common-js";
import {RabbitBinding} from "./models/RabbitBinding";
import {RabbitExchange} from "./models/RabbitExchange";
import {RabbitQueue} from "./models/RabbitQueue";
import {RabbitNode} from "./models/RabbitNode";
import {Routing} from "./Routing";
import {RoutingOverride} from "./models/RoutingOverride";

@Injectable()
export class RabbitService extends BaseHttp2Service {
	constructor(http : Http) { super (http); }

	getRabbitNodes() : Observable<RabbitNode[]> {
		return this.httpGet('api/rabbit/nodes');
	}

	pingRabbitNode(address:string) : Observable<RabbitNode> {
		let params = new URLSearchParams();
		params.set('address', address);
		return this.httpGet('api/rabbit/ping', {search : params});
	}

	getRabbitQueues(address:string) : Observable<RabbitQueue[]> {
		let params = new URLSearchParams();
		params.set('address', address);
		return this.httpGet('api/rabbit/queues', {search : params});
	}

	getRabbitExchanges(address:string) : Observable<RabbitExchange[]> {
		let params = new URLSearchParams();
		params.set('address', address);
		return this.httpGet('api/rabbit/exchanges', {search : params});
	}

	getRabbitBindings(address:string) : Observable<RabbitBinding[]> {
		let params = new URLSearchParams();
		params.set('address', address);
		return this.httpGet('api/rabbit/bindings', {search : params});
	}

	synchronize(address:string) : Observable<any> {
		return this.httpPost('api/rabbit/synchronize', address );
	}

	getRoutings() : Observable<Routing[]> {
		return this.httpGet('api/rabbit/routings');
	}

	saveRoutings(routings : Routing[]) : Observable<any> {
		console.log('saving routings');
		console.log(routings);

		return this.httpPost('api/rabbit/routings', routings);
	}

	getRoutingOverrides(): Observable<RoutingOverride[]> {
		return this.httpGet('api/rabbit/overrides');
	}

	saveRoutingOverrides(overrides : RoutingOverride[]) : Observable<any> {
		console.log('saving routing overrides');
		console.log(overrides);

		return this.httpPost('api/rabbit/overrides', overrides);
	}
}
