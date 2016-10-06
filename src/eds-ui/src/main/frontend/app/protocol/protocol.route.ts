export class protocolRoute {
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
					template: require('./protocol.html'),
					controller: 'ProtocolController',
					controllerAs: 'protocolCtrl'
				}
			}
		];
	}
}
