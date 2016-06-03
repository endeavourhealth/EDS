/// <reference path="../../typings/tsd.d.ts" />
var app;
(function (app) {
    var protocol;
    (function (protocol) {
        'use strict';
        var protocolRoute = (function () {
            function protocolRoute(stateProvider) {
                var routes = protocolRoute.getRoutes();
                routes.forEach(function (route) {
                    stateProvider.state(route.state, route.config);
                });
            }
            protocolRoute.getRoutes = function () {
                return [
                    {
                        state: 'app.protocolAction',
                        config: {
                            url: '/protocol/:itemUuid/:itemAction',
                            templateUrl: 'app/protocol/protocol.html',
                            controller: 'ProtocolController',
                            controllerAs: 'protocolCtrl'
                        }
                    }
                ];
            };
            protocolRoute.$inject = ['$stateProvider'];
            return protocolRoute;
        })();
        angular
            .module('app.protocol')
            .config(protocolRoute);
    })(protocol = app.protocol || (app.protocol = {}));
})(app || (app = {}));
//# sourceMappingURL=protocol.route.js.map