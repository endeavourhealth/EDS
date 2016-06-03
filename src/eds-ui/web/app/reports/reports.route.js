/// <reference path="../../typings/tsd.d.ts" />
var app;
(function (app) {
    var reports;
    (function (reports) {
        'use strict';
        var ReportsRoute = (function () {
            function ReportsRoute(stateProvider) {
                var routes = ReportsRoute.getRoutes();
                routes.forEach(function (route) {
                    stateProvider.state(route.state, route.config);
                });
            }
            ReportsRoute.getRoutes = function () {
                return [
                    {
                        state: 'app.reportList',
                        config: {
                            url: '/reportList',
                            templateUrl: 'app/reports/reportList.html',
                            controller: 'ReportListController',
                            controllerAs: 'ctrl'
                        }
                    },
                    {
                        state: 'app.reportAction',
                        config: {
                            url: '/report/:itemUuid/:itemAction',
                            templateUrl: 'app/reports/report.html',
                            controller: 'ReportController',
                            controllerAs: 'reportCtrl'
                        }
                    }
                ];
            };
            ReportsRoute.$inject = ['$stateProvider'];
            return ReportsRoute;
        })();
        angular
            .module('app.reports')
            .config(ReportsRoute);
    })(reports = app.reports || (app.reports = {}));
})(app || (app = {}));
//# sourceMappingURL=reports.route.js.map