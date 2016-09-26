/// <reference path="../../typings/index.d.ts" />
/// <reference path="../core/patientIdentity.service.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.recordViewer {
	import IRecordViewerService = app.core.IRecordViewerService;
	import Patient = app.models.Patient;
	import Service = app.models.Service;
	import System = app.models.System;
	import IServiceService = app.service.IServiceService;
    import PatientFindController = app.dialogs.PatientFindController;
    import Encounter = app.models.Encounter;

	'use strict';

	export class RecordViewerController {
        patient: Patient;
        encounters: Encounter[];

		static $inject = ['$uibModal', 'RecordViewerService', 'LoggerService', 'ServiceService', '$state'];

		constructor(private $modal: IModalService,
        		    protected recordViewerService: IRecordViewerService,
					protected logger: ILoggerService,
					protected serviceService: IServiceService,
					protected $state: IStateService) {

            this.showPatientFind();
		}

        showPatientFind() {
            var vm = this;
            PatientFindController.open(vm.$modal)
                .result.then(function (result: Patient) {
                vm.patient = result;
            });
        }

        clearPatient() {
            this.patient = null;
            this.encounters = null;
        }

        loadConsultations() {
            var vm = this;
            vm.encounters = null;
            vm.recordViewerService.getEncounters(vm.patient.serviceId, vm.patient.systemId, vm.patient.patientId)
                .then(function (data: Encounter[]) {
                    vm.encounters = data;
                    if (data == null) {
                        vm.logger.error('No patient found');
                    }
                });
        }
    }

	angular
		.module('app.recordViewer')
		.controller('RecordViewerController', RecordViewerController);
}
