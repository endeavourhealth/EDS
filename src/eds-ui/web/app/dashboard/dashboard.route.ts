/// <reference path="../../typings/tsd.d.ts" />

module app.dashboard {
	'use strict';

	class DashboardRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = DashboardRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app.dashboard',
					config: {
						url: '/dashboard',
						templateUrl: 'app/dashboard/dashboard.html',
						controller: 'DashboardController',
						controllerAs: 'dashboard'
					}
				}
			];
		}
	}

	angular
		.module('app.dashboard')
		.config(DashboardRoute);

}