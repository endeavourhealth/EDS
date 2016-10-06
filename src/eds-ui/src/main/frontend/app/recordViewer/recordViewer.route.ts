export class RecordViewerRoute {
	static $inject = ['$stateProvider'];

	constructor(stateProvider:angular.ui.IStateProvider) {
		var routes = RecordViewerRoute.getRoutes();

		routes.forEach(function (route) {
			stateProvider.state(route.state, route.config);
		});
	}

	static getRoutes() {
		return [
			{
				state: 'app.recordViewer',
				config: {
					url: '/recordViewer',
					template: require('./recordViewer.html'),
					controller: 'RecordViewerController',
					controllerAs: 'ctrl'
				}
			}
		];
	}
}
