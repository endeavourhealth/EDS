import {Component} from "@angular/core";
import {Organisation} from "./models/Organisation";
import {Service} from "../services/models/Service";
import {AdminService} from "../administration/admin.service";
import {OrganisationService} from "./organisation.service";
import {LoggerService} from "../common/logger.service";
import {Transition, StateService} from "ui-router-ng2";
import {ServicePickerDialog} from "../services/servicePicker.dialog";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
	template: require('./organisationEditor.html')
})
export class OrganisationEditComponent {

	organisation : Organisation = <Organisation>{};
	services : Service[];

	constructor(private $modal: NgbModal,
							private state : StateService,
							private log:LoggerService,
							private adminService : AdminService,
							private organisationService : OrganisationService,
							private transition : Transition
	) {
		this.performAction(transition.params()['itemAction'], transition.params()['itemUuid']);
	}

	protected performAction(action:string, itemUuid:string) {
		switch (action) {
			case 'add':
				this.create(itemUuid);
				break;
			case 'edit':
				this.load(itemUuid);
				break;
		}
	}

	create(uuid : string) {
		this.organisation = {
			uuid : uuid,
			name : ''
		} as Organisation;
	}

	load(uuid : string) {
		var vm = this;
		vm.organisationService.getOrganisation(uuid)
			.subscribe(result =>  {
				vm.organisation = result;
				vm.getOrganisationServices();
			},
			error => vm.log.error('Error loading', error, 'Error')
			);
	}

	save(close : boolean) {
		var vm = this;

		// Populate service organisations before save
		vm.organisation.services = {};
		for (var idx in this.services) {
			var service : Service = this.services[idx];
			this.organisation.services[service.uuid] = service.name;
		}

		vm.organisationService.saveOrganisation(vm.organisation)
			.subscribe(saved => {
				vm.organisation.uuid = saved.uuid;
				vm.adminService.clearPendingChanges();
				vm.log.success('Item saved', vm.organisation, 'Saved');
				if (close) { vm.state.go(vm.transition.from()); }
			},
			error => vm.log.error('Error saving', error, 'Error')
			);
	}

	close() {
		this.adminService.clearPendingChanges();
		this.state.go(this.transition.from());
	}

	private getOrganisationServices() {
		var vm = this;
		vm.organisationService.getOrganisationServices(vm.organisation.uuid)
			.subscribe(
				result => vm.services = result,
				error => vm.log.error('Failed to load organisation services', error, 'Load organisation services')
			);
	}

	private editServices() {
		var vm = this;
		ServicePickerDialog.open(vm.$modal, vm.services)
			.result.then(function (result : Service[]) {
				vm.services = result;
		});
	}
}
