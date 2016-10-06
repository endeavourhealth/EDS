export class PatientIdentityRoute {
	static $inject = ['$stateProvider'];

	constructor(stateProvider:angular.ui.IStateProvider) {
		var routes = PatientIdentityRoute.getRoutes();

		routes.forEach(function (route) {
			stateProvider.state(route.state, route.config);
		});
	}

	static getRoutes() {
		return [
			{
				state: 'app.patientIdentity',
				config: {
					url: '/patientIdentity',
					template: require('./patientIdentity.html'),
					controller: 'PatientIdentityController',
					controllerAs: 'patientIdentityCtrl'
				}
			}
		];
	}
}
