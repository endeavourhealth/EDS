/// <reference path="../../typings/index.d.ts" />

module app.core {
    'use strict';

    export interface IMedicalRecordService {
    }

    export class MedicalRecordService extends BaseHttpService implements IMedicalRecordService {

    }

    angular
        .module('app.core')
        .service('MedicalRecordService', MedicalRecordService);
}