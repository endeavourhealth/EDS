/// <reference path="../../typings/tsd.d.ts" />

module app.query {
	'use strict';

	class QueryRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = QueryRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app.query',
					config: {
						url: '/query',
						templateUrl: 'app/query/query.html',
						controller: 'QueryController',
						controllerAs: 'query'
					}
				},
				{
					state: 'app.queryAction',
					config: {
						url: '/query/:itemUuid/:itemAction',
						templateUrl: 'app/query/query.html',
						controller: 'QueryController',
						controllerAs: 'query'
					}
				}
			];
		}
	}

	angular
		.module('app.query')
		.config(QueryRoute);

}