import {StateProvider} from "angular-ui-router";

export class ConsentRoute {
    static $inject = ['$stateProvider'];

    constructor(stateProvider:StateProvider) {
        var routes = ConsentRoute.getRoutes();

        routes.forEach(function (route) {
            stateProvider.state(route.state, route.config);
        });
    }

    static getRoutes() {
        return [
            {
                state: 'app.consent',
                config: {
                    url: '/consent',
                    template: require('./consent.html'),
                    controller: 'ConsentController',
                    controllerAs: 'ctrl'
                }
            }
        ];
    }
}
