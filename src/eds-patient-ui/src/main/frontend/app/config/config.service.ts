import {ConfigurationResource} from "./ConfigurationResource";
import {BaseHttpService} from "../core/baseHttp.service";

export class ConfigService extends BaseHttpService {

	getConfig(configurationId : string):ng.IPromise<ConfigurationResource> {
		var request = {
			params: {
				'configurationId': configurationId
			}
		};

		return this.httpGet('api/config/getConfig', request);
	}

	saveConfig(configResource : ConfigurationResource):ng.IPromise<ConfigurationResource> {
		return this.httpPost('api/config/saveConfig', configResource);
	}
}
