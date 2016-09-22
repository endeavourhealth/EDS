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

	'use strict';

	export class RecordViewerController {
        patientFindSelection: Patient;
		patient: Patient;

		static $inject = ['$uibModal', 'RecordViewerService', 'LoggerService', 'ServiceService', '$state'];

		constructor(private $modal : IModalService,
        		    protected recordViewerService: IRecordViewerService,
					protected logger:ILoggerService,
					protected serviceService : IServiceService,
					protected $state : IStateService) {

            this.showPatientFind();
		}

		refresh() {
            this.getDemographics();
		}

		getDemographics() {
			var vm = this;
			vm.patient = null;
			vm.recordViewerService.getDemographics(vm.patientFindSelection.serviceId, vm.patientFindSelection.systemId, vm.patientFindSelection.patientId)
				.then(function (data: Patient) {
					vm.patient = data;
					if (data == null) {
						vm.logger.error('No patient found');
					}
				});
		}

        showPatientFind() {
            var vm = this;
            PatientFindController.open(vm.$modal)
                .result.then(function (result: Patient) {
                vm.patientFindSelection = result;
                vm.refresh();
            });
        }

        clearPatient() {
            this.patientFindSelection = null;
            this.patient = null;
        }
	}

	angular
		.module('app.recordViewer')
		.controller('RecordViewerController', RecordViewerController);
}
