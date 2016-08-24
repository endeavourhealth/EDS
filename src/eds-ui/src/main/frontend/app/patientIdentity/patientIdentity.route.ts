/// <reference path="../../typings/index.d.ts" />

module app.patientIdentity {
	'use strict';

	class PatientIdentityRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = PatientIdentityRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app.patientIdentity',
					config: {
						url: '/patientIdentity',
						templateUrl: 'app/patientIdentity/patientIdentity.html',
						controller: 'PatientIdentityController',
						controllerAs: 'patientIdentityCtrl'
					}
				}
			];
		}
	}

	angular
		.module('app.patientIdentity')
		.config(PatientIdentityRoute);

}