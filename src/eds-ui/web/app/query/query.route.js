/// <reference path="../../typings/tsd.d.ts" />
var app;
(function (app) {
    var query;
    (function (query) {
        'use strict';
        var QueryRoute = (function () {
            function QueryRoute(stateProvider) {
                var routes = QueryRoute.getRoutes();
                routes.forEach(function (route) {
                    stateProvider.state(route.state, route.config);
                });
            }
            QueryRoute.getRoutes = function () {
                return [
                    {
                        state: 'app.query',
                        config: {
                            url: '/query',
                            templateUrl: 'app/query/query.html',
                            controller: 'QueryController',
                            controllerAs: 'query'
                        }
                    },
                    {
                        state: 'app.queryAction',
                        config: {
                            url: '/query/:itemUuid/:itemAction',
                            templateUrl: 'app/query/query.html',
                            controller: 'QueryController',
                            controllerAs: 'query'
                        }
                    }
                ];
            };
            QueryRoute.$inject = ['$stateProvider'];
            return QueryRoute;
        })();
        angular
            .module('app.query')
            .config(QueryRoute);
    })(query = app.query || (app.query = {}));
})(app || (app = {}));
//# sourceMappingURL=query.route.js.map