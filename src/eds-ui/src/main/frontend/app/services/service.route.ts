export class ServiceRoute {
	static $inject = ['$stateProvider'];

	constructor(stateProvider:angular.ui.IStateProvider) {
		var routes = ServiceRoute.getRoutes();

		routes.forEach(function (route) {
			stateProvider.state(route.state, route.config);
		});
	}

	static getRoutes() {
		return [
			{
				state: 'app.service',
				config: {
					url: '/service',
					template: require('./list/serviceList.html'),
					controller: 'ServiceListController',
					controllerAs: 'ctrl'
				}
			},
			{
				state: 'app.serviceAction',
				config: {
					url: '/service/:itemUuid/:itemAction',
					template: require('./editor/serviceEditor.html'),
					controller: 'ServiceEditorController',
					controllerAs: 'ctrl'
				}
			}
		];
	}
}
