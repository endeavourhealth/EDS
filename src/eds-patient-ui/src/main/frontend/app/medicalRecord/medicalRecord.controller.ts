/// <reference path="../../typings/index.d.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.medicalRecord {
    import IMedicalRecordService = app.core.IMedicalRecordService;
    import ILoggerService = app.blocks.ILoggerService;

    'use strict';

    class MedicalRecordController {

        static $inject = ['MedicalRecordService', 'LoggerService', '$state'];

        constructor(private medicalRecordService:IMedicalRecordService,
                    private logger:ILoggerService,
                    private $state : IStateService) {
            //this.refresh();
        }
    }

    angular
        .module('app.medicalRecord')
        .controller('MedicalRecordController', MedicalRecordController);
}
