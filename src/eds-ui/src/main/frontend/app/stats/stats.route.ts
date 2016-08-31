/// <reference path="../../typings/index.d.ts" />

module app.stats {
	'use strict';

	class statsRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = statsRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app.stats',
					config: {
						url: '/stats',
						templateUrl: 'app/stats/stats.html',
						controller: 'StatsController',
						controllerAs: 'statsCtrl'
					}
				}
			];
		}
	}

	angular
		.module('app.stats')
		.config(statsRoute);

}