export class TransformErrorsRoute {
	static $inject = ['$stateProvider'];

	constructor(stateProvider:angular.ui.IStateProvider) {
		var routes = TransformErrorsRoute.getRoutes();

		routes.forEach(function (route) {
			stateProvider.state(route.state, route.config);
		});
	}

	static getRoutes() {
		return [
			{
				state: 'app.transformErrors',
				config: {
					url: '/transformErrors',
					template: require('./transformErrors.html'),
					controller: 'TransformErrorsController',
					controllerAs: 'TransformErrorsCtrl'
				}
			}
		];
	}
}
