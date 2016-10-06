import IQService = angular.IQService;

import {RouteGroup} from "../RouteGroup";
import {IConfigService} from "../../config/config.service";
import {ConfigurationResource} from "../../models/ConfigurationResource";

export interface IRouteGroupService {
	getRouteGroups(): ng.IPromise<RouteGroup[]>;
	saveRouteGroups(routeGroups : RouteGroup[]):any;
}

export class RouteGroupService implements IRouteGroupService {

	static $inject = ['$q', 'ConfigService'];
	private configurationId = "b9b14e26-5a52-4f36-ad89-f01e465c1361";
	private configurationName = "RouteGroup";

	constructor(protected promise : IQService, protected configService:IConfigService) {
	}

	getRouteGroups(): ng.IPromise<RouteGroup[]> {
		var defer = this.promise.defer();
		this.configService.getConfig(this.configurationId)
			.then(function(configResource : ConfigurationResource) {
				defer.resolve(angular.fromJson(configResource.configurationData));
			})
			.catch(function(exception) {
				defer.reject(exception);
			});

		return defer.promise;
	}

	saveRouteGroups(routeGroups : RouteGroup[]) {
		var configurationResource : ConfigurationResource = {
			configurationId : this.configurationId,
			configurationName : this.configurationName,
			configurationData : angular.toJson(routeGroups)
		};
		var defer = this.promise.defer();
		this.configService.saveConfig(configurationResource)
			.then(function() {
				defer.resolve();
			})
			.catch(function(exception) {
				defer.reject(exception);
			});

		return defer.promise;
	}
}
