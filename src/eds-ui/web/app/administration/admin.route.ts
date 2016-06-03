/// <reference path="../../typings/tsd.d.ts" />

module app.admin {
	'use strict';

	class AdminRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = AdminRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app.admin',
					config: {
						url: '/admin',
						templateUrl: 'app/administration/admin.html',
						controller: 'AdminController',
						controllerAs: 'admin'
					}
				}
			];
		}
	}

	angular
		.module('app.admin')
		.config(AdminRoute);

}