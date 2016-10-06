export class LibraryRoute {
	static $inject = ['$stateProvider'];

	constructor(stateProvider:angular.ui.IStateProvider) {
		var routes = LibraryRoute.getRoutes();

		routes.forEach(function (route) {
			stateProvider.state(route.state, route.config);
		});
	}

	static getRoutes() {
		return [
			{
				state: 'app.library',
				config: {
					url: '/library',
					template: require('./library.html'),
					controller: 'LibraryController',
					controllerAs: 'ctrl'
				}
			}
		];
	}
}
