/// <reference path="../../typings/index.d.ts" />
/// <reference path="../core/patientIdentity.service.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.recordViewer {
	import IRecordViewerService = app.core.IRecordViewerService;
	import Patient = app.models.Patient;
	import Service = app.models.Service;
	import System = app.models.System;
	import IServiceService = app.service.IServiceService;

	'use strict';

	export class RecordViewerController {
        serviceId : string;
        systemId : string;
        patientId : string;
		patient: Patient;

		static $inject = ['RecordViewerService', 'LoggerService', 'ServiceService', '$state'];

		constructor(protected recordViewerService: IRecordViewerService,
					protected logger:ILoggerService,
					protected serviceService : IServiceService,
					protected $state : IStateService) {

            // temporarily fix these until patient find is present
            this.serviceId = "db7eba14-4a89-4090-abf8-af6c60742cb1";
            this.systemId = "db8fa60e-08ff-4b61-ba4c-6170e6cb8df7";
            this.patientId = "81b66483-ac01-47dd-98a7-73b4b5639680";

            this.refresh();
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
	}

	angular
		.module('app.recordViewer')
		.controller('RecordViewerController', RecordViewerController);
}
