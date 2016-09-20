/// <reference path="../../typings/index.d.ts" />

module app.consent {
    'use strict';

    class ConsentRoute {
        static $inject = ['$stateProvider'];

        constructor(stateProvider:angular.ui.IStateProvider) {
            var routes = ConsentRoute.getRoutes();

            routes.forEach(function (route) {
                stateProvider.state(route.state, route.config);
            });
        }

        static getRoutes() {
            return [
                {
                    state: 'app.consent',
                    config: {
                        url: '/consent',
                        templateUrl: 'app/consent/consent.html',
                        controller: 'ConsentController',
                        controllerAs: 'ctrl'
                    }
                }
            ];
        }
    }

    angular
        .module('app.consent')
        .config(ConsentRoute);

}