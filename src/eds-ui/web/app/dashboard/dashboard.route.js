/// <reference path="../../typings/tsd.d.ts" />
var app;
(function (app) {
    var dashboard;
    (function (dashboard) {
        'use strict';
        var DashboardRoute = (function () {
            function DashboardRoute(stateProvider) {
                var routes = DashboardRoute.getRoutes();
                routes.forEach(function (route) {
                    stateProvider.state(route.state, route.config);
                });
            }
            DashboardRoute.getRoutes = function () {
                return [
                    {
                        state: 'app.dashboard',
                        config: {
                            url: '/dashboard',
                            templateUrl: 'app/dashboard/dashboard.html',
                            controller: 'DashboardController',
                            controllerAs: 'dashboard'
                        }
                    }
                ];
            };
            DashboardRoute.$inject = ['$stateProvider'];
            return DashboardRoute;
        })();
        angular
            .module('app.dashboard')
            .config(DashboardRoute);
    })(dashboard = app.dashboard || (app.dashboard = {}));
})(app || (app = {}));
//# sourceMappingURL=dashboard.route.js.map