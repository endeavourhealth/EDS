/// <reference path="../../typings/index.d.ts" />
/// <reference path="../core/resources.service.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.resources {
	import IResourcesService = app.core.IResourcesService;
	import FhirResourceContainer = app.models.FhirResourceContainer;
	import FhirResourceType = app.models.FhirResourceType;

	'use strict';

	export class ResourcesController {
		resourceContainers : FhirResourceContainer[];
		patientId : string;
		patientResourceTypes : FhirResourceType[];
		patientResourceTypeSelected : string;
		resourceId : string;
		allResourceTypes : FhirResourceType[];
		resourceTypeSelected : string;



		static $inject = ['ResourcesService', 'LoggerService', '$state'];

		constructor(protected resourcesService:IResourcesService,
					protected logger:ILoggerService,
					protected $state : IStateService) {

			this.getAllResourceTypes()

			//this.refresh();
		}

		getAllResourceTypes() {
			var vm = this;
			vm.resourcesService.getAllResourceTypes()
				.then(function(result) {
					vm.allResourceTypes = result;
				})
				.catch(function (error) {
					vm.logger.error('Failed to retrieve all resource types', error, 'Resource Types');
				});
		}

		getResourceTypesForPatient() {
			var vm = this;
			vm.resourcesService.getResourceTypesForPatient(vm.patientId)
				.then(function(result) {
					vm.patientResourceTypes = result;
				})
				.catch(function (error) {
					vm.logger.error('Failed to retrieve resource types for patient', error, 'Resource Types');
				});
		}

		getResourceForId() {
			var vm = this;
			vm.resourcesService.getResourceForId(vm.resourceTypeSelected, vm.resourceId)
				.then(function(result) {
					vm.resourceContainers = result;
				})
				.catch(function (error) {
					vm.logger.error('Failed to retrieve resources for ID', error, 'Resource Types');
				});
		}

		getResourcesForPatient() {
			var vm = this;
			vm.resourcesService.getResourcesForPatient(vm.patientResourceTypeSelected, vm.patientId)
				.then(function(result) {
					vm.resourceContainers = result;
				})
				.catch(function (error) {
					vm.logger.error('Failed to retrieve resources for patient', error, 'Resource Types');
				});
		}

	}

	angular
		.module('app.resources')
		.controller('ResourcesController', ResourcesController);
}
