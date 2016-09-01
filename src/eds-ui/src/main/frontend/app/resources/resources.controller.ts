/// <reference path="../../typings/index.d.ts" />
/// <reference path="../core/resources.service.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.resources {
	import IResourcesService = app.core.IResourcesService;
	import FhirResourceContainer = app.models.FhirResourceContainer;
	import FhirResourceType = app.models.FhirResourceType;
	import IScope = angular.IScope;

	'use strict';

	export class ResourcesController {

		//variables for patient ID searching
		patientId : string;
		patientResourceTypes : FhirResourceType[];
		patientResourceTypeSelected : string;

		//variables for single resource searching
		resourceId : string;
		allResourceTypes : FhirResourceType[];
		resourceTypeSelected : string;

		//search results
		searchPerformed : boolean;
		resourceContainers : FhirResourceContainer[];
		resources : Object[];


		static $inject = ['ResourcesService', 'LoggerService', '$scope', '$state'];



		constructor(protected resourcesService:IResourcesService,
					protected logger:ILoggerService,
					protected $scope : any,
					protected $state : IStateService) {

			this.getAllResourceTypes();

			//add these functions to the scope, so they can be used from within the HTML
			$scope.isArray = angular.isArray;
			$scope.isObject = angular.isObject;

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
					vm.parseResourceContainers();
					console.log('Retrieved ' + vm.resourceContainers.length + ' resources for id');
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
					vm.parseResourceContainers();
					console.log('Retrieved ' + vm.resourceContainers.length + ' resources for patient');
				})
				.catch(function (error) {
					vm.logger.error('Failed to retrieve resources for patient', error, 'Resource Types');
				});
		}

		parseResourceContainers() {

			var vm = this;
			vm.resources = new Array<Object>();

			for (var i=0; i<vm.resourceContainers.length; i++) {
				var container = vm.resourceContainers[i];
				var json = container.resourceJson;
				var resource = JSON.parse(json);
				vm.resources.push(resource);
			}
		}

	}

	angular
		.module('app.resources')
		.controller('ResourcesController', ResourcesController);
}
