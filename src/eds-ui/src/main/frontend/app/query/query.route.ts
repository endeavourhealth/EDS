export class QueryRoute {
	static $inject = ['$stateProvider'];

	constructor(stateProvider:angular.ui.IStateProvider) {
		var routes = QueryRoute.getRoutes();

		routes.forEach(function (route) {
			stateProvider.state(route.state, route.config);
		});
	}

	static getRoutes() {
		return [
			{
				state: 'app.query',
				config: {
					url: '/query',
					template: require('./query.html'),
					controller: 'QueryController',
					controllerAs: 'query'
				}
			},
			{
				state: 'app.queryAction',
				config: {
					url: '/query/:itemUuid/:itemAction',
					template: require('./query.html'),
					controller: 'QueryController',
					controllerAs: 'query'
				}
			}
		];
	}
}
