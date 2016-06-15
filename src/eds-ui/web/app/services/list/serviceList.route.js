/// <reference path="../../../typings/tsd.d.ts" />
var app;
(function (app) {
    var service;
    (function (service) {
        'use strict';
        var ServiceRoute = (function () {
            function ServiceRoute(stateProvider) {
                var routes = ServiceRoute.getRoutes();
                routes.forEach(function (route) {
                    stateProvider.state(route.state, route.config);
                });
            }
            ServiceRoute.getRoutes = function () {
                return [
                    {
                        state: 'app.service',
                        config: {
                            url: '/service',
                            templateUrl: 'app/services/list/serviceList.html',
                            controller: 'ServiceListController',
                            controllerAs: 'ctrl'
                        }
                    }
                ];
            };
            ServiceRoute.$inject = ['$stateProvider'];
            return ServiceRoute;
        })();
        angular
            .module('app.service')
            .config(ServiceRoute);
    })(service = app.service || (app.service = {}));
})(app || (app = {}));
//# sourceMappingURL=serviceList.route.js.map