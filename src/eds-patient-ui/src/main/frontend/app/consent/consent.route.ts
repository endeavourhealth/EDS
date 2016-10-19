import {StateProvider} from "angular-ui-router";

export class ConsentRoute {
    static $inject = ['$stateProvider'];

    constructor(stateProvider:StateProvider) {
        var routes = ConsentRoute.getRoutes();

        routes.forEach(function (route) {
            stateProvider.state(route);
        });
    }

    static getRoutes() {
        return [
            {
                name: 'app.consent',
                url: '/consent',
                component: 'consentComponent'
            }
        ];
    }
}
