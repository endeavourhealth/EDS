/// <reference path="../../typings/index.d.ts" />

module app.resources {
	'use strict';

	class ResourcesRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = ResourcesRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app.resources',
					config: {
						url: '/resources',
						templateUrl: 'app/resources/resources.html',
						controller: 'ResourcesController',
						controllerAs: 'resourcesCtrl'
					}
				}
			];
		}
	}

	angular
		.module('app.resources')
		.config(ResourcesRoute);

}