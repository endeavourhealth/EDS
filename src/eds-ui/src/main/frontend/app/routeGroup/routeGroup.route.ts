export class RouteGroupRoute {
	static $inject = ['$stateProvider'];

	constructor(stateProvider:angular.ui.IStateProvider) {
		var routes = RouteGroupRoute.getRoutes();

		routes.forEach(function (route) {
			stateProvider.state(route.state, route.config);
		});
	}

	static getRoutes() {
		return [
			{
				state: 'app.routeGroup',
				config: {
					url: '/routeGroup',
					template: require('./list/routeGroupList.html'),
					controller: 'RouteGroupListController',
					controllerAs: 'ctrl'
				}
			}
		];
	}
}
