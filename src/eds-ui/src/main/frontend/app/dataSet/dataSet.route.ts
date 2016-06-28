/// <reference path="../../typings/index.d.ts" />

module app.dataSet {
	'use strict';

	class dataSetRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = dataSetRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app.dataSetAction',
					config: {
						url: '/dataSet/:itemUuid/:itemAction',
						templateUrl: 'app/dataSet/dataSet.html',
						controller: 'dataSetController',
						controllerAs: 'dataSetCtrl'
					}
				}
			];
		}
	}

	angular
		.module('app.dataSet')
		.config(dataSetRoute);

}