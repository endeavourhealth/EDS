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
        serviceId : string;
        systemId : string;
        patientId : string;
		patient: Patient;

		static $inject = ['$uibModal', 'RecordViewerService', 'LoggerService', 'ServiceService', '$state'];

		constructor(private $modal : IModalService,
        		    protected recordViewerService: IRecordViewerService,
					protected logger:ILoggerService,
					protected serviceService : IServiceService,
					protected $state : IStateService) {

            // temporarily fix these until patient find is present
            this.serviceId = "35663c09-0a0e-45c3-9989-e00334225906";
            this.systemId = "d96d21de-0576-471a-91f3-7fe0116213a9";
            this.patientId = "62ed352e-10f9-4680-83d9-ac055eb0afab";

            this.showPatientFind();
		}

		refresh() {
            this.getDemographics();
		}

		getDemographics() {
			var vm = this;
			vm.patient = null;
			vm.recordViewerService.getDemographics(vm.serviceId, vm.systemId, vm.patientId)
				.then(function (data:Patient) {
					vm.patient = data;
					if (data == null) {
						vm.logger.error('No patient found');
					}
				});
		}

        private showPatientFind() {
            var vm = this;
            PatientFindController.open(vm.$modal)
                .result.then(function (result: Service[]) {
                vm.refresh();
            });
        }
	}

	angular
		.module('app.recordViewer')
		.controller('RecordViewerController', RecordViewerController);
}
