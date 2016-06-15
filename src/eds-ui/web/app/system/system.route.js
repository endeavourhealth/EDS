/// <reference path="../../typings/tsd.d.ts" />
var app;
(function (app) {
    var system;
    (function (system) {
        'use strict';
        var systemRoute = (function () {
            function systemRoute(stateProvider) {
                var routes = systemRoute.getRoutes();
                routes.forEach(function (route) {
                    stateProvider.state(route.state, route.config);
                });
            }
            systemRoute.getRoutes = function () {
                return [
                    {
                        state: 'app.systemAction',
                        config: {
                            url: '/system/:itemUuid/:itemAction',
                            templateUrl: 'app/system/system.html',
                            controller: 'SystemController',
                            controllerAs: 'systemCtrl'
                        }
                    }
                ];
            };
            systemRoute.$inject = ['$stateProvider'];
            return systemRoute;
        })();
        angular
            .module('app.system')
            .config(systemRoute);
    })(system = app.system || (app.system = {}));
})(app || (app = {}));
//# sourceMappingURL=system.route.js.map