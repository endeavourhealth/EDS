export class OrganisationSetRoute {
	static $inject = ['$stateProvider'];

	constructor(stateProvider:angular.ui.IStateProvider) {
		var routes = OrganisationSetRoute.getRoutes();

		routes.forEach(function (route) {
			stateProvider.state(route.state, route.config);
		});
	}

	static getRoutes() {
		return [
			{
				state: 'app.organisationSet',
				config: {
					url: '/organisationSet',
					template: require('./organisationSet.html'),
					controller: 'OrganisationSetController',
					controllerAs: 'ctrl'
				}
			}
		];
	}
}