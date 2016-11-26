import {FhirResourceType} from "./models/FhirResourceType";
import {FhirResourceContainer} from "./models/FhirResourceContainer";
import {ResourcesService} from "./resources.service";
import {LoggerService} from "../common/logger.service";
import {Transition} from "ui-router-ng2";
import {Component} from "@angular/core";

@Component({
	template : require('./resources.html')
})
export class ResourcesComponent {

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

	constructor(protected resourcesService:ResourcesService,
				protected logger:LoggerService,
				protected transition : Transition) {

		this.getAllResourceTypes();

		this.performAction(transition.params()['itemAction'], transition.params()['itemUuid']);
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
			.subscribe(
				(result) => vm.allResourceTypes = result,
				(error) => vm.logger.error('Failed to retrieve all resource types', error, 'Resource Types')
			);
	}

	/*getResourceTypesForPatient() {
		var vm = this;
		vm.resourcesService.getResourceTypesForPatient(vm.patientId)
			.subscribe(
			(result) => vm.patientResourceTypes = result,
			(error) => vm.logger.error('Failed to retrieve resource types for patient', error, 'Resource Types')
			);
	}*/

	getResourceForId() {
		var vm = this;

		vm.resourcesService.getResourceForId(vm.resourceTypeSelected, vm.resourceId)
			.subscribe(
				(result) => {
					vm.resourceContainers = result;
					vm.parseResourceContainers();
					console.log('Retrieved ' + vm.resourceContainers.length + ' resources for id');
				},
				(error) => vm.logger.error('Failed to retrieve resources for ID', error, 'Resource Types')
			);
	}

	getResourcesForPatient() {
		var vm = this;
		vm.resourcesService.getResourcesForPatient(vm.patientResourceTypeSelected, vm.patientId)
			.subscribe(
				(result) => {
					vm.resourceContainers = result;
					vm.parseResourceContainers();
					console.log('Retrieved ' + vm.resourceContainers.length + ' resources for patient');
				},
				(error) => vm.logger.error('Failed to retrieve resources for patient', error, 'Resource Types')
			);
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
