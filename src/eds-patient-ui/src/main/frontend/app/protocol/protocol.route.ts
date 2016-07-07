/// <reference path="../../typings/index.d.ts" />

module app.protocol {
	'use strict';

	class protocolRoute {
		static $inject = ['$stateProvider'];

		constructor(stateProvider:angular.ui.IStateProvider) {
			var routes = protocolRoute.getRoutes();

			routes.forEach(function (route) {
				stateProvider.state(route.state, route.config);
			});
		}

		static getRoutes() {
			return [
				{
					state: 'app.protocolAction',
					config: {
						url: '/protocol/:itemUuid/:itemAction',
						templateUrl: 'app/protocol/protocol.html',
						controller: 'ProtocolController',
						controllerAs: 'protocolCtrl'
					}
				}
			];
		}
	}

	angular
		.module('app.protocol')
		.config(protocolRoute);

}