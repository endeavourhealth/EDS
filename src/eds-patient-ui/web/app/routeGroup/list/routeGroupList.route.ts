/// <reference path="../../../typings/tsd.d.ts" />

module app.routeGroup {
	'use strict';

	class RouteGroupRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = RouteGroupRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app.routeGroup',
					config: {
						url: '/routeGroup',
						templateUrl: 'app/routeGroup/list/routeGroupList.html',
						controller: 'RouteGroupListController',
						controllerAs: 'ctrl'
					}
				}
			];
		}
	}

	angular
		.module('app.routeGroup')
		.config(RouteGroupRoute);

}