import {StateProvider} from "angular-ui-router";

export class AppRoute {
	static $inject = ['$stateProvider'];

	constructor(stateProvider:StateProvider) {
		var routes = AppRoute.getRoutes();

		routes.forEach(function (route) {
			stateProvider.state(route);
		});
	}

	static getRoutes() {
		return [
			{
				name: 'app',
				url: '/app',
				component: 'shellComponent',
			}
		];
	}
}
