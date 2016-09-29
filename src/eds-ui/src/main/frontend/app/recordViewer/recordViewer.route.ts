/// <reference path="../../typings/index.d.ts" />

module app.recordViewer {
	'use strict';

	class RecordViewerRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = RecordViewerRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app.recordViewer',
					config: {
						url: '/recordViewer',
						templateUrl: 'app/recordViewer/recordViewer.html',
						controller: 'RecordViewerController',
						controllerAs: 'ctrl'
					}
				}
			];
		}
	}

	angular
		.module('app.recordViewer')
		.config(RecordViewerRoute);

}