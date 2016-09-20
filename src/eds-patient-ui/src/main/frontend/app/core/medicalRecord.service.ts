/// <reference path="../../typings/index.d.ts" />

module app.core {
    'use strict';
    import PatientService = app.models.PatientService;

    export interface IMedicalRecordService {
        getServices() : ng.IPromise<PatientService[]>;
    }

    export class MedicalRecordService extends BaseHttpService implements IMedicalRecordService {
        getServices(): ng.IPromise<PatientService[]> {
            return this.httpGet('api/medicalRecord/getServices');
        }
    }

    angular
        .module('app.core')
        .service('MedicalRecordService', MedicalRecordService);
}