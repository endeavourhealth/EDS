export class AdminRoute {
	static $inject = ['$stateProvider'];

	constructor(stateProvider:angular.ui.IStateProvider) {
		var routes = AdminRoute.getRoutes();

		routes.forEach(function (route) {
			stateProvider.state(route.state, route.config);
		});
	}

	static getRoutes() {
		return [
			{
				state: 'app.admin',
				config: {
					url: '/admin',
					template: require('./admin.html'),
					controller: 'AdminController',
					controllerAs: 'admin'
				}
			}
		];
	}
}
