/// <reference path="../../typings/tsd.d.ts" />
var app;
(function (app) {
    var codeSet;
    (function (codeSet) {
        'use strict';
        var CodeSetRoute = (function () {
            function CodeSetRoute(stateProvider) {
                var routes = CodeSetRoute.getRoutes();
                routes.forEach(function (route) {
                    stateProvider.state(route.state, route.config);
                });
            }
            CodeSetRoute.getRoutes = function () {
                return [
                    {
                        state: 'app.codeSetAction',
                        config: {
                            url: '/codeSet/:itemUuid/:itemAction',
                            templateUrl: 'app/codeSet/codeSet.html',
                            controller: 'CodeSetController',
                            controllerAs: 'codeSetCtrl'
                        }
                    }
                ];
            };
            CodeSetRoute.$inject = ['$stateProvider'];
            return CodeSetRoute;
        })();
        angular
            .module('app.codeSet')
            .config(CodeSetRoute);
    })(codeSet = app.codeSet || (app.codeSet = {}));
})(app || (app = {}));
//# sourceMappingURL=codeSet.route.js.map