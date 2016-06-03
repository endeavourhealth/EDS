/// <reference path="../../typings/tsd.d.ts" />

module app.reports {
	'use strict';

	class ReportsRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = ReportsRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app.reportList',
					config: {
						url: '/reportList',
						templateUrl: 'app/reports/reportList.html',
						controller: 'ReportListController',
						controllerAs: 'ctrl'
					}
				},
				{
					state: 'app.reportAction',
					config: {
						url: '/report/:itemUuid/:itemAction',
						templateUrl: 'app/reports/report.html',
						controller: 'ReportController',
						controllerAs: 'reportCtrl'
					}
				}
			];
		}
	}

	angular
		.module('app.reports')
		.config(ReportsRoute);

}