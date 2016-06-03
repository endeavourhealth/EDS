/// <reference path="../typings/tsd.d.ts" />
var app;
(function (app) {
    'use strict';
    var AppRoute = (function () {
        function AppRoute(stateProvider) {
            var routes = AppRoute.getRoutes();
            routes.forEach(function (route) {
                stateProvider.state(route.state, route.config);
            });
        }
        AppRoute.getRoutes = function () {
            return [
                {
                    state: 'app',
                    config: {
                        url: '/app',
                        templateUrl: 'app/layout/shell.html',
                    }
                }
            ];
        };
        AppRoute.$inject = ['$stateProvider'];
        return AppRoute;
    })();
    angular
        .module('app')
        .config(AppRoute);
})(app || (app = {}));
//# sourceMappingURL=app.route.js.map