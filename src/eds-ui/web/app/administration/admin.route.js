/// <reference path="../../typings/tsd.d.ts" />
var app;
(function (app) {
    var admin;
    (function (admin) {
        'use strict';
        var AdminRoute = (function () {
            function AdminRoute(stateProvider) {
                var routes = AdminRoute.getRoutes();
                routes.forEach(function (route) {
                    stateProvider.state(route.state, route.config);
                });
            }
            AdminRoute.getRoutes = function () {
                return [
                    {
                        state: 'app.admin',
                        config: {
                            url: '/admin',
                            templateUrl: 'app/administration/admin.html',
                            controller: 'AdminController',
                            controllerAs: 'admin'
                        }
                    }
                ];
            };
            AdminRoute.$inject = ['$stateProvider'];
            return AdminRoute;
        })();
        angular
            .module('app.admin')
            .config(AdminRoute);
    })(admin = app.admin || (app.admin = {}));
})(app || (app = {}));
//# sourceMappingURL=admin.route.js.map