/// <reference path="../../typings/tsd.d.ts" />
var app;
(function (app) {
    var library;
    (function (library) {
        'use strict';
        var LibraryRoute = (function () {
            function LibraryRoute(stateProvider) {
                var routes = LibraryRoute.getRoutes();
                routes.forEach(function (route) {
                    stateProvider.state(route.state, route.config);
                });
            }
            LibraryRoute.getRoutes = function () {
                return [
                    {
                        state: 'app.library',
                        config: {
                            url: '/library',
                            templateUrl: 'app/library/library.html',
                            controller: 'LibraryController',
                            controllerAs: 'ctrl'
                        }
                    }
                ];
            };
            LibraryRoute.$inject = ['$stateProvider'];
            return LibraryRoute;
        })();
        angular
            .module('app.library')
            .config(LibraryRoute);
    })(library = app.library || (app.library = {}));
})(app || (app = {}));
//# sourceMappingURL=library.route.js.map