import {StateProvider} from "angular-ui-router";

export class MedicalRecordRoute {
    static $inject = ['$stateProvider'];

    constructor(stateProvider:StateProvider) {
        var routes = MedicalRecordRoute.getRoutes();

        routes.forEach(function (route) {
            stateProvider.state(route);
        });
    }

    static getRoutes() {
        return [
            {
                name: 'app.medicalRecord',
                url: '/medicalRecord',
                component: 'medicalRecordComponent'
            }
        ];
    }
}
