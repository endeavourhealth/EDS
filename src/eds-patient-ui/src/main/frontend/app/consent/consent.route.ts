export class ConsentRoute {
    static $inject = ['$stateProvider'];

    constructor(stateProvider:angular.ui.IStateProvider) {
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
