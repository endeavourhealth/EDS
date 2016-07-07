/// <reference path="../../../typings/index.d.ts" />

module app.service {
	'use strict';

	class ServiceRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = ServiceRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app.service',
					config: {
						url: '/service',
						templateUrl: 'app/services/list/serviceList.html',
						controller: 'ServiceListController',
						controllerAs: 'ctrl'
					}
				}
			];
		}
	}

	angular
		.module('app.service')
		.config(ServiceRoute);

}