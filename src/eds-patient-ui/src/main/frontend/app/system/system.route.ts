/// <reference path="../../typings/index.d.ts" />

module app.system {
	'use strict';

	class systemRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = systemRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app.systemAction',
					config: {
						url: '/system/:itemUuid/:itemAction',
						templateUrl: 'app/system/system.html',
						controller: 'SystemController',
						controllerAs: 'systemCtrl'
					}
				}
			];
		}
	}

	angular
		.module('app.system')
		.config(systemRoute);

}