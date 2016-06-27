/// <reference path="../../typings/tsd.d.ts" />

module app.listOutput {
	'use strict';

	class ListOutputRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = ListOutputRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app.listOutputAction',
					config: {
						url: '/listOutput/:itemUuid/:itemAction',
						templateUrl: 'app/listOutput/listOutput.html',
						controller: 'ListOutputController',
						controllerAs: 'listOutputCtrl'
					}
				}
			];
		}
	}

	angular
		.module('app.listOutput')
		.config(ListOutputRoute);

}