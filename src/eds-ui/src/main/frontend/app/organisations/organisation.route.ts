export class OrganisationRoute {
	static $inject = ['$stateProvider'];

	constructor(stateProvider:angular.ui.IStateProvider) {
		var routes = OrganisationRoute.getRoutes();

		routes.forEach(function (route) {
			stateProvider.state(route.state, route.config);
		});
	}

	static getRoutes() {
		return [
			{
				state: 'app.organisation',
				config: {
					url: '/organisation',
					template: require('./list/organisationList.html'),
					controller: 'OrganisationListController',
					controllerAs: 'ctrl'
				}
			},
			{
				state: 'app.organisationAction',
				config: {
					url: '/organisation/:itemUuid/:itemAction',
					template: require('./editor/organisationEditor.html'),
					controller: 'OrganisationEditorController',
					controllerAs: 'ctrl'
				}
			}
		];
	}
}
