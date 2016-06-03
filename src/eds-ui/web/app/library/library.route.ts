/// <reference path="../../typings/tsd.d.ts" />

module app.library {
	'use strict';

	class LibraryRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = LibraryRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app.library',
					config: {
						url: '/library',
						templateUrl: 'app/library/library.html',
						controller: 'LibraryController',
						controllerAs: 'ctrl'
					}
				}
			];
		}
	}

	angular
		.module('app.library')
		.config(LibraryRoute);

}