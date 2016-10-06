import {FhirResourceType} from "../models/FhirResourceType";
import {FhirResourceContainer} from "../models/FhirResourceContainer";
import {IResourcesService} from "../core/resources.service";
import {ILoggerService} from "../blocks/logger.service";

export class ResourcesController {

	//variables for patient ID searching
	patientId : string;
	//patientResourceTypes : FhirResourceType[];
	patientResourceTypeSelected : string;

	//variables for single resource searching
	resourceId : string;
	allResourceTypes : FhirResourceType[];
	resourceTypeSelected : string;

	//search results
	searchPerformed : boolean;
	resourceContainers : FhirResourceContainer[];
	resources : Object[];


	static $inject = ['ResourcesService', 'LoggerService', '$scope', '$stateParams'];

	constructor(protected resourcesService:IResourcesService,
				protected logger:ILoggerService,
				protected $scope : any,
				protected $stateParams : {itemAction : string, itemUuid : string}) {

		this.getAllResourceTypes();

		//add these functions to the scope, so they can be used from within the HTML
		$scope.isArray = angular.isArray;
		$scope.isObject = angular.isObject;

		this.performAction($stateParams.itemAction, $stateParams.itemUuid);

	}

	protected performAction(action:string, itemUuid:string) {
		switch (action) {
			case 'patient':
				this.resourceId = itemUuid;
				this.resourceTypeSelected = "Patient";

				this.patientResourceTypeSelected = "Patient";
				this.patientId = itemUuid;

				this.getResourcesForPatient();
				break;
		}
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

	/*getResourceTypesForPatient() {
		var vm = this;
		vm.resourcesService.getResourceTypesForPatient(vm.patientId)
			.then(function(result) {
				vm.patientResourceTypes = result;
			})
			.catch(function (error) {
				vm.logger.error('Failed to retrieve resource types for patient', error, 'Resource Types');
			});
	}*/

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
