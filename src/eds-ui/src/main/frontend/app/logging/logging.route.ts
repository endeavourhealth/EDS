/// <reference path="../../typings/index.d.ts" />

module app.logging {
	'use strict';

	class loggingRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = loggingRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app.logging',
					config: {
						url: '/logging',
						templateUrl: 'app/logging/logging.html',
						controller: 'LoggingController',
						controllerAs: 'loggingCtrl'
					}
				}
			];
		}
	}

	angular
		.module('app.logging')
		.config(loggingRoute);

}