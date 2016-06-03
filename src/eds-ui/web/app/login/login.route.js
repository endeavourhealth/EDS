/// <reference path="../../typings/tsd.d.ts" />
var app;
(function (app) {
    var login;
    (function (login) {
        'use strict';
        var LoginRoute = (function () {
            function LoginRoute(stateProvider) {
                var routes = LoginRoute.getRoutes();
                routes.forEach(function (route) {
                    stateProvider.state(route.state, route.config);
                });
            }
            LoginRoute.getRoutes = function () {
                return [
                    {
                        state: 'login',
                        config: {
                            url: '/',
                            templateUrl: 'app/login/login.html',
                            controller: 'LoginController',
                            controllerAs: 'login',
                            unsecured: true,
                            resolve: {
                                userName: function () { return ''; }
                            }
                        }
                    }
                ];
            };
            LoginRoute.$inject = ['$stateProvider'];
            return LoginRoute;
        })();
        angular
            .module('app.login')
            .config(LoginRoute);
    })(login = app.login || (app.login = {}));
})(app || (app = {}));
//# sourceMappingURL=login.route.js.map