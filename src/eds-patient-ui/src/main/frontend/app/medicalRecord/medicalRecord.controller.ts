/// <reference path="../../typings/index.d.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.medicalRecord {
    import IMedicalRecordService = app.core.IMedicalRecordService;
    import ILoggerService = app.blocks.ILoggerService;

    'use strict';
    import PatientService = app.models.PatientService;

    class MedicalRecordController {
        static $inject = ['MedicalRecordService', 'LoggerService', '$state'];

        services : PatientService[];
        selectedService : string;

        constructor(private medicalRecordService:IMedicalRecordService,
                    private logger:ILoggerService,
                    private $state : IStateService) {
            this.loadServiceList();
        }

        loadServiceList() {
            var vm = this;
            vm.services = null;
            vm.medicalRecordService.getServices()
              .then(function(data : PatientService[])
              {
                 vm.services = data;
              }
            );
        }
    }

    angular
        .module('app.medicalRecord')
        .controller('MedicalRecordController', MedicalRecordController);
}
