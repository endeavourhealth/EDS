/// <reference path="../../../typings/index.d.ts" />

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
						templateUrl: 'app/organisations/list/organisationList.html',
						controller: 'OrganisationListController',
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