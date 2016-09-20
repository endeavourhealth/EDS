/// <reference path="../../typings/index.d.ts" />

module app.audit {
    'use strict';

    class AuditRoute {
        static $inject = ['$stateProvider'];

        constructor(stateProvider:angular.ui.IStateProvider) {
            var routes = AuditRoute.getRoutes();

            routes.forEach(function (route) {
                stateProvider.state(route.state, route.config);
            });
        }

        static getRoutes() {
            return [
                {
                    state: 'app.audit',
                    config: {
                        url: '/audit',
                        templateUrl: 'app/audit/audit.html',
                        controller: 'AuditController',
                        controllerAs: 'ctrl'
                    }
                }
            ];
        }
    }

    angular
        .module('app.audit')
        .config(AuditRoute);

}