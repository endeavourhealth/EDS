/// <reference path="../../../typings/tsd.d.ts" />

module app.organisation {
	'use strict';

	class OrganisationRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = OrganisationRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app.organisation',
					config: {
						url: '/organisation',
						templateUrl: 'app/organisation/list/organisationList.html',
						controller: 'OrganiastionListController',
						controllerAs: 'ctrl'
					}
				}
			];
		}
	}

	angular
		.module('app.organisation')
		.config(OrganisationRoute);

}