/// <reference path="../../typings/tsd.d.ts" />

module app.organisationSet {
	'use strict';

	class OrganisationSetRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = OrganisationSetRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app.organisationSet',
					config: {
						url: '/organisationSet',
						templateUrl: 'app/organisationSet/organisationSet.html',
						controller: 'OrganisationSetController',
						controllerAs: 'ctrl'
					}
				}
			];
		}
	}

	angular
		.module('app.organisationSet')
		.config(OrganisationSetRoute);

}