import {ConfigurationResource} from "./ConfigurationResource";
import {Http} from "@angular/http";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {Observable} from "rxjs";

export class ConfigService extends BaseHttp2Service {

	constructor(http : Http) { super (http); }

	getConfig(configurationId : string):Observable<ConfigurationResource> {
		var request = {
			params: {
				'configurationId': configurationId
			}
		};

		return this.httpGet('api/config/getConfig', request);
	}

	saveConfig(configResource : ConfigurationResource):Observable<ConfigurationResource> {
		return this.httpPost('api/config/saveConfig', configResource);
	}
}
