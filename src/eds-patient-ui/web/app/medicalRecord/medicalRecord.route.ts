/// <reference path="../../typings/tsd.d.ts" />

module app.medicalRecord {
    'use strict';

    class MedicalRecordRoute {
        static $inject = ['$stateProvider'];

        constructor(stateProvider:angular.ui.IStateProvider) {
            var routes = MedicalRecordRoute.getRoutes();

            routes.forEach(function (route) {
                stateProvider.state(route.state, route.config);
            });
        }

        static getRoutes() {
            return [
                {
                    state: 'app.medicalRecord',
                    config: {
                        url: '/medicalRecord',
                        templateUrl: 'app/medicalRecord/medicalRecord.html',
                        controller: 'MedicalRecordController',
                        controllerAs: 'medicalRecord'
                    }
                }
            ];
        }
    }

    angular
        .module('app.medicalRecord')
        .config(MedicalRecordRoute);

}