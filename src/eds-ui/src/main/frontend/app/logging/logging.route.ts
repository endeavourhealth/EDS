export class LoggingRoute {
	static $inject = ['$stateProvider'];

	constructor(stateProvider:angular.ui.IStateProvider) {
		var routes = LoggingRoute.getRoutes();

		routes.forEach(function (route) {
			stateProvider.state(route.state, route.config);
		});
	}

	static getRoutes() {
		return [
			{
				state: 'app.logging',
				config: {
					url: '/logging',
					template: require('./logging.html'),
					controller: 'LoggingController',
					controllerAs: 'loggingCtrl'
				}
			}
		];
	}
}
