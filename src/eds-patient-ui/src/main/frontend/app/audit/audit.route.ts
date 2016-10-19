import {StateProvider} from "angular-ui-router";

export class AuditRoute {
    static $inject = ['$stateProvider'];

    constructor(stateProvider:StateProvider) {
        var routes = AuditRoute.getRoutes();

        routes.forEach(function (route) {
            stateProvider.state(route);
        });
    }

    static getRoutes() {
        return [
            {
                name: 'app.audit',
                url: '/audit',
                component: 'auditComponent'
            }
        ];
    }
}
