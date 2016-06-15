/// <reference path="../../../typings/tsd.d.ts" />
var app;
(function (app) {
    var routeGroup;
    (function (routeGroup) {
        'use strict';
        var RouteGroupRoute = (function () {
            function RouteGroupRoute(stateProvider) {
                var routes = RouteGroupRoute.getRoutes();
                routes.forEach(function (route) {
                    stateProvider.state(route.state, route.config);
                });
            }
            RouteGroupRoute.getRoutes = function () {
                return [
                    {
                        state: 'app.routeGroup',
                        config: {
                            url: '/routeGroup',
                            templateUrl: 'app/routeGroup/list/routeGroupList.html',
                            controller: 'RouteGroupListController',
                            controllerAs: 'ctrl'
                        }
                    }
                ];
            };
            RouteGroupRoute.$inject = ['$stateProvider'];
            return RouteGroupRoute;
        })();
        angular
            .module('app.routeGroup')
            .config(RouteGroupRoute);
    })(routeGroup = app.routeGroup || (app.routeGroup = {}));
})(app || (app = {}));
//# sourceMappingURL=routeGroupList.route.js.map