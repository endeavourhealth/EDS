/// <reference path="../../../typings/tsd.d.ts" />
var app;
(function (app) {
    var organisation;
    (function (organisation) {
        'use strict';
        var OrganisationRoute = (function () {
            function OrganisationRoute(stateProvider) {
                var routes = OrganisationRoute.getRoutes();
                routes.forEach(function (route) {
                    stateProvider.state(route.state, route.config);
                });
            }
            OrganisationRoute.getRoutes = function () {
                return [
                    {
                        state: 'app.organisation',
                        config: {
                            url: '/organisation',
                            templateUrl: 'app/organisations/list/organisationList.html',
                            controller: 'OrganisationListController',
                            controllerAs: 'ctrl'
                        }
                    }
                ];
            };
            OrganisationRoute.$inject = ['$stateProvider'];
            return OrganisationRoute;
        })();
        angular
            .module('app.organisation')
            .config(OrganisationRoute);
    })(organisation = app.organisation || (app.organisation = {}));
})(app || (app = {}));
//# sourceMappingURL=organisationList.route.js.map