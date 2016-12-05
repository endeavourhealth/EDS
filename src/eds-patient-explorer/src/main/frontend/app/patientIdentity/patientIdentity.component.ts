import {PatientIdentity} from "./PatientIdentity";
import {PatientIdentityService} from "./patientIdentity.service";
import {LoggerService} from "../common/logger.service";
import {Component} from "@angular/core";
import {StateService} from "ui-router-ng2";
import {Service} from "./models/Service";
import {System} from "./models/System";

@Component({
	template : require('./patientIdentity.html')
})
export class PatientIdentityComponent {
	patientIdentities:PatientIdentity[];
	services : Service[];
	serviceId : string;
	systems : System[];
	systemId : string;
	localId : string;
	nhsNumber : string;
	patientId : string;

	constructor(protected patientIdentityService:PatientIdentityService,
				protected logger:LoggerService,
				protected $state : StateService) {
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
		vm.patientIdentityService.getServices()
			.subscribe(
				(result) => vm.services = result,
				(error) => vm.logger.error('Failed to load services', error, 'Load services')
			);
	}

	loadSystems() {
		var vm = this;
		vm.patientIdentityService.getSystemsForService(vm.serviceId)
			.subscribe(
				(result) => vm.systems = result,
				(error) => vm.logger.error('Failed to load systems', error, 'Load systems')
			);
	}

	getPatientIdentitiesByPatientId() {
		var vm = this;
		vm.patientIdentities = null;
		vm.patientIdentityService.getByPatientId(vm.patientId)
			.subscribe(
				(data) => {
					vm.patientIdentities = data;
					if (data.length == 0) {
						vm.logger.error('No results found');
					}
				}
			);
	}

	getPatientIdentitiedByNhsNumber() {
		var vm = this;
		vm.patientIdentities = null;
		vm.patientIdentityService.getByNhsNumber(vm.nhsNumber)
			.subscribe(
				(data) => {
					vm.patientIdentities = data;
					if (data.length == 0) {
						vm.logger.error('No results found');
					}
				}
			);
	}

	getPatientIdentitiesByLocalIdentifier() {
		var vm = this;
		vm.patientIdentities = null;
		vm.patientIdentityService.getByLocalIdentifier(vm.serviceId, vm.systemId, vm.localId)
			.subscribe(
				(data) => {
					vm.patientIdentities = data;
					if (data.length == 0) {
						vm.logger.error('No results found');
					}
				}
			);
	}

	/*actionItem(event : PatientIdentityEvent, action : string) {
		alert(action+" : "+event.loggerName);
	}*/

	actionItem(uuid : string, action : string) {
		this.$state.go('app.resourceEdit', {itemUuid: uuid, itemAction: action});
	}
}
