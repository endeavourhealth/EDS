export class CodeSetRoute {
	static $inject = ['$stateProvider'];

	constructor(stateProvider:angular.ui.IStateProvider) {
		var routes = CodeSetRoute.getRoutes();

		routes.forEach(function (route) {
			stateProvider.state(route.state, route.config);
		});
	}

	static getRoutes() {
		return [
			{
				state: 'app.codeSetAction',
				config: {
					url: '/codeSet/:itemUuid/:itemAction',
					template: require('./codeSet.html'),
					controller: 'CodeSetController',
					controllerAs: 'codeSetCtrl'
				}
			}
		];
	}
}
