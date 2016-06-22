/// <reference path="../typings/tsd.d.ts" />

module app {
	'use strict';

	class AppRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = AppRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app',
					config: {
						url: '/app',
						templateUrl: 'app/layout/shell.html',
					}
				}
			];
		}
	}

	angular
		.module('app')
		.config(AppRoute);

}