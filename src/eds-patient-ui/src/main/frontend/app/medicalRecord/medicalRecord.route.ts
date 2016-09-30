import {IStateProvider} from "angular-ui-router";

export class MedicalRecordRoute {
    static $inject = ['$stateProvider'];

    constructor(stateProvider:IStateProvider) {
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
                    template: require('./medicalRecord.html'),
                    controller: 'MedicalRecordController',
                    controllerAs: 'ctrl'
                }
            }
        ];
    }
}
