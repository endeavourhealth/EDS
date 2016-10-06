export class DashboardRoute {
	static $inject = ['$stateProvider'];

	constructor(stateProvider:angular.ui.IStateProvider) {
		var routes = DashboardRoute.getRoutes();

		routes.forEach(function (route) {
			stateProvider.state(route.state, route.config);
		});
	}

	static getRoutes() {
		return [
			{
				state: 'app.dashboard',
				config: {
					url: '/dashboard',
					template: require('./dashboard.html'),
					controller: 'DashboardController',
					controllerAs: 'dashboard'
				}
			}
		];
	}
}
