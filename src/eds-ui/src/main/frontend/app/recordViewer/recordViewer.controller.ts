/// <reference path="../../typings/index.d.ts" />
/// <reference path="../core/patientIdentity.service.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.recordViewer {
	import IPatientIdentityService = app.core.IPatientIdentityService;
	import PatientIdentity = app.models.PatientIdentity;
	import Service = app.models.Service;
	import System = app.models.System;
	import IServiceService = app.service.IServiceService;

	'use strict';

	export class RecordViewerController {
		patientIdentities:PatientIdentity[];
		services : Service[];
		serviceId : string;
		systems : System[];
		systemId : string;
		localId : string;
		nhsNumber : string;
		patientId : string;

		static $inject = ['PatientIdentityService', 'LoggerService', 'ServiceService', '$state'];

		constructor(protected patientIdentityService:IPatientIdentityService,
					protected logger:ILoggerService,
					protected serviceService : IServiceService,
					protected $state : IStateService) {
			this.loadServices();
			this.refresh();
		}

		refresh() {
			/*var vm = this;
			var serviceName = $("#service>option:selected").html()
			this.getPatientIdentityEvents(vm.serviceId);*/
		}

		loadServices() {
			var vm = this;
			vm.serviceService.getAll()
				.then(function(result) {
					vm.services = result;
				})
				.catch(function (error) {
					vm.logger.error('Failed to load services', error, 'Load services');
				});
		}

		loadSystems() {
			var vm = this;
			vm.serviceService.getSystemsForService(vm.serviceId)
				.then(function(result) {
					vm.systems = result;
				})
				.catch(function (error) {
					vm.logger.error('Failed to load systems', error, 'Load systems');
				});
		}

		getPatientIdentitiesByPatientId() {
			var vm = this;
			vm.patientIdentities = null;
			vm.patientIdentityService.getByPatientId(vm.patientId)
				.then(function (data:PatientIdentity[]) {
					vm.patientIdentities = data;
					if (data.length == 0) {
						vm.logger.error('No results found');
					}
				});
		}

		getPatientIdentitiedByNhsNumber() {
			var vm = this;
			vm.patientIdentities = null;
			vm.patientIdentityService.getByNhsNumber(vm.nhsNumber)
				.then(function (data:PatientIdentity[]) {
					vm.patientIdentities = data;
					if (data.length == 0) {
						vm.logger.error('No results found');
					}
				});
		}

		getPatientIdentitiesByLocalIdentifier() {
			var vm = this;
			vm.patientIdentities = null;
			vm.patientIdentityService.getByLocalIdentifier(vm.serviceId, vm.systemId, vm.localId)
				.then(function (data:PatientIdentity[]) {
					vm.patientIdentities = data;
					if (data.length == 0) {
						vm.logger.error('No results found');
					}
				});
		}

		/*actionItem(event : PatientIdentityEvent, action : string) {
			alert(action+" : "+event.loggerName);
		}*/
	}

	angular
		.module('app.recordViewer')
		.controller('RecordViewerController', RecordViewerController);
}
