export class systemRoute {
	static $inject = ['$stateProvider'];

	constructor(stateProvider:angular.ui.IStateProvider) {
		var routes = systemRoute.getRoutes();

		routes.forEach(function (route) {
			stateProvider.state(route.state, route.config);
		});
	}

	static getRoutes() {
		return [
			{
				state: 'app.systemAction',
				config: {
					url: '/system/:itemUuid/:itemAction',
					template: require('./system.html'),
					controller: 'SystemController',
					controllerAs: 'systemCtrl'
				}
			}
		];
	}
}
