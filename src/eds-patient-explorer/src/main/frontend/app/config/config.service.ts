import {BaseHttp2Service} from "../core/baseHttp2.service";
import {ConfigurationResource} from "./models/ConfigurationResource";
import {Injectable} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";

@Injectable()
export class ConfigService extends BaseHttp2Service {
	constructor(http : Http) { super (http); }

	getConfig(configurationId : string) : Observable<ConfigurationResource> {
		let params = new URLSearchParams();
		params.set('configurationId',configurationId);

		return this.httpGet('api/config/getConfig', { search : params });
	}

	saveConfig(configResource : ConfigurationResource):Observable<ConfigurationResource> {
		return this.httpPost('api/config/saveConfig', configResource);
	}
}
