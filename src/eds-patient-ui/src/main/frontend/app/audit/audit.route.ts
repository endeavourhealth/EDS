import {StateProvider} from "angular-ui-router";

export class AuditRoute {
    static $inject = ['$stateProvider'];

    constructor(stateProvider:StateProvider) {
        var routes = AuditRoute.getRoutes();

        routes.forEach(function (route) {
            stateProvider.state(route.state, route.config);
        });
    }

    static getRoutes() {
        return [
            {
                state: 'app.audit',
                config: {
                    url: '/audit',
                    template: require('./audit.html'),
                    controller: 'AuditController',
                    controllerAs: 'ctrl'
                }
            }
        ];
    }
}
