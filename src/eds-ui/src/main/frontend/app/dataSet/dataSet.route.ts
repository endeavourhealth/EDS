export class DataSetRoute {
	static $inject = ['$stateProvider'];

	constructor(stateProvider:angular.ui.IStateProvider) {
		var routes = DataSetRoute.getRoutes();

		routes.forEach(function (route) {
			stateProvider.state(route.state, route.config);
		});
	}

	static getRoutes() {
		return [
			{
				state: 'app.dataSetAction',
				config: {
					url: '/dataSet/:itemUuid/:itemAction',
					template: require('./dataSet.html'),
					controller: 'DataSetController',
					controllerAs: 'dataSetCtrl'
				}
			}
		];
	}
}
