/// <reference path="../../typings/tsd.d.ts" />

module app.config {
	'use strict';

	export interface IConfigService {
		getConfig(configurationId : string):ng.IPromise<ConfigurationResource>;
		saveConfig(configResource : ConfigurationResource):ng.IPromise<ConfigurationResource>;
	}

	export class ConfigService extends core.BaseHttpService implements IConfigService {

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

	angular
		.module('app.config')
		.service('ConfigService', ConfigService);
}