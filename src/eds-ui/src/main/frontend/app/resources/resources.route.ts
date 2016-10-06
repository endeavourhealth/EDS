export class ResourcesRoute {
	static $inject = ['$stateProvider'];

	constructor(stateProvider:angular.ui.IStateProvider) {
		var routes = ResourcesRoute.getRoutes();

		routes.forEach(function (route) {
			stateProvider.state(route.state, route.config);
		});
	}

	static getRoutes() {
		return [
			{
				state: 'app.resources',
				config: {
					url: '/resources/:itemUuid/:itemAction',
					template: require('./resources.html'),
					controller: 'ResourcesController',
					controllerAs: 'resourcesCtrl'
				}
			}
		];
	}
}
