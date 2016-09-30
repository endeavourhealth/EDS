import {IStateProvider} from "angular-ui-router";

export class AppRoute {
	static $inject = ['$stateProvider'];

	constructor(stateProvider:IStateProvider) {
		var routes = AppRoute.getRoutes();

		routes.forEach(function (route) {
			stateProvider.state(route.state, route.config);
		});
	}

	static getRoutes() {
		return [
			{
				state: 'app',
				config: {
					url: '/app',
					template: require('./layout/shell.html'),
				}
			}
		];
	}
}
