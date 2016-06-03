/// <reference path="../../typings/tsd.d.ts" />

module app.codeSet {
	'use strict';

	class CodeSetRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = CodeSetRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app.codeSetAction',
					config: {
						url: '/codeSet/:itemUuid/:itemAction',
						templateUrl: 'app/codeSet/codeSet.html',
						controller: 'CodeSetController',
						controllerAs: 'codeSetCtrl'
					}
				}
			];
		}
	}

	angular
		.module('app.codeSet')
		.config(CodeSetRoute);

}