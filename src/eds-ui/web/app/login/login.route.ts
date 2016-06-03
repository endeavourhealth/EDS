/// <reference path="../../typings/tsd.d.ts" />

module app.login {
	'use strict';

	class LoginRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = LoginRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'login',
					config: {
						url: '/',
						templateUrl: 'app/login/login.html',
						controller: 'LoginController',
						controllerAs: 'login',
						unsecured: true,
						resolve: {
							userName: () => ''
						}
					}
				}
			];
		}
	}

	angular
		.module('app.login')
		.config(LoginRoute);

}