/// <reference path="../../../typings/tsd.d.ts" />

module app.core {
	import RouteGroup = app.routeGroup.RouteGroup;
	'use strict';

	export interface IRouteGroupService {
		getRouteGroups():ng.IPromise<RouteGroup[]>;
		saveRouteGroups(routeGroups : RouteGroup[]):ng.IPromise<RouteGroup[]>;
	}

	export class RouteGroupService extends BaseHttpService implements IRouteGroupService {

		getRouteGroups():ng.IPromise<RouteGroup[]> {
			return this.httpGet('api/config/getRouteGroups');
		}

		saveRouteGroups(routeGroups : RouteGroup[]) {
			return this.httpPost('api/config/saveRouteGroups', routeGroups);
		}
	}

	angular
		.module('app.routeGroup')
		.service('RouteGroupService', RouteGroupService);
}