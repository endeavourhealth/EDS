/// <reference path="../../typings/tsd.d.ts" />
var app;
(function (app) {
    var listOutput;
    (function (listOutput) {
        'use strict';
        var ListOutputRoute = (function () {
            function ListOutputRoute(stateProvider) {
                var routes = ListOutputRoute.getRoutes();
                routes.forEach(function (route) {
                    stateProvider.state(route.state, route.config);
                });
            }
            ListOutputRoute.getRoutes = function () {
                return [
                    {
                        state: 'app.listOutputAction',
                        config: {
                            url: '/listOutput/:itemUuid/:itemAction',
                            templateUrl: 'app/listOutput/listOutput.html',
                            controller: 'ListOutputController',
                            controllerAs: 'listOutputCtrl'
                        }
                    }
                ];
            };
            ListOutputRoute.$inject = ['$stateProvider'];
            return ListOutputRoute;
        })();
        angular
            .module('app.listOutput')
            .config(ListOutputRoute);
    })(listOutput = app.listOutput || (app.listOutput = {}));
})(app || (app = {}));
//# sourceMappingURL=listOutput.route.js.map