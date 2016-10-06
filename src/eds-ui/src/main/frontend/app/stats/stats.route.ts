export class statsRoute {
	static $inject = ['$stateProvider'];

	constructor(stateProvider:angular.ui.IStateProvider) {
		var routes = statsRoute.getRoutes();

		routes.forEach(function (route) {
			stateProvider.state(route.state, route.config);
		});
	}

	static getRoutes() {
		return [
			{
				state: 'app.stats',
				config: {
					url: '/stats',
					template: require('./stats.html'),
					controller: 'StatsController',
					controllerAs: 'statsCtrl'
				}
			}
		];
	}
}
