/// <reference path="../../typings/tsd.d.ts" />
var app;
(function (app) {
    var organisationSet;
    (function (organisationSet) {
        'use strict';
        var OrganisationSetRoute = (function () {
            function OrganisationSetRoute(stateProvider) {
                var routes = OrganisationSetRoute.getRoutes();
                routes.forEach(function (route) {
                    stateProvider.state(route.state, route.config);
                });
            }
            OrganisationSetRoute.getRoutes = function () {
                return [
                    {
                        state: 'app.organisationSet',
                        config: {
                            url: '/organisationSet',
                            templateUrl: 'app/organisationSet/organisationSet.html',
                            controller: 'OrganisationSetController',
                            controllerAs: 'ctrl'
                        }
                    }
                ];
            };
            OrganisationSetRoute.$inject = ['$stateProvider'];
            return OrganisationSetRoute;
        })();
        angular
            .module('app.organisationSet')
            .config(OrganisationSetRoute);
    })(organisationSet = app.organisationSet || (app.organisationSet = {}));
})(app || (app = {}));
//# sourceMappingURL=organisationSet.route.js.map