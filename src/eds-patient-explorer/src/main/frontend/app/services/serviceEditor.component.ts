import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService, Transition} from "ui-router-ng2";

import {Service} from "./models/Service";
import {Organisation} from "../organisations/models/Organisation";
import {System} from "../systems/models/System";
import {TechnicalInterface} from "../systems/models/TechnicalInterface";
import {Endpoint} from "./models/Endpoint";
import {AdminService} from "../administration/admin.service";
import {LibraryService} from "../library/library.service";
import {ServiceService} from "./service.service";
import {OrganisationPickerDialog} from "../organisations/organisationPicker.dialog";
import {MessageBoxDialog} from "../dialogs/messageBox/messageBox.dialog";
import {LoggerService} from "../common/logger.service";

@Component({
	template : require('./serviceEditor.html')
})
export class ServiceEditComponent {

	service : Service = <Service>{};
	organisations : Organisation[];
	systems : System[];
	technicalInterfaces : TechnicalInterface[];

	selectedEndpoint : Endpoint = <Endpoint>{};

	constructor(private $modal : NgbModal,
							private $window : StateService,
							private log:LoggerService,
							private adminService : AdminService,
							private libraryService : LibraryService,
							private serviceService : ServiceService,
							private transition : Transition) {

		this.loadSystems();
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
		this.service = {
			uuid : uuid,
			name : 'New item',
			endpoints : []
		} as Service;
	}

	load(uuid : string) {
		var vm = this;
		vm.serviceService.get(uuid)
			.subscribe(
				(result) => {
					vm.service = result;
					vm.getServiceOrganisations();
				},
				(error) => vm.log.error('Error loading', error, 'Error')
			);
	}

	save(close : boolean) {
		var vm = this;

		// Populate service organisations before save
		vm.service.organisations = {};
		for (var idx in this.organisations) {
			var organisation: Organisation = this.organisations[idx];
			this.service.organisations[organisation.uuid] = organisation.name;
		}

		vm.serviceService.save(vm.service)
			.subscribe(
				(saved) => {
					vm.service.uuid = saved.uuid;
					vm.adminService.clearPendingChanges();
					vm.log.success('Item saved', vm.service, 'Saved');
					if (close) {
						vm.$window.go(vm.transition.from());
					}
				},
				(error) => vm.log.error('Error saving', error, 'Error')
			);
	}

	close() {
		this.adminService.clearPendingChanges();
		this.$window.go(this.transition.from());
	}

	private addEndpoint() {
		var newEndpoint = {
			endpoint : "http://"
		} as Endpoint;
		this.service.endpoints.push(newEndpoint);
		this.selectedEndpoint = newEndpoint;
	}

	removeEndpoint(scope : any) {
		this.service.endpoints.splice(scope.$index, 1);
		if (this.selectedEndpoint === scope.item) {
			this.selectedEndpoint = null;
		}
	}

	private getServiceOrganisations() {
		var vm = this;
		vm.serviceService.getServiceOrganisations(vm.service.uuid)
			.subscribe(
				(result) => vm.organisations = result,
				(error) => vm.log.error('Failed to load service organisations', error, 'Load service organisations')
			);
	}

	private getSystem(systemUuid : string) : System {
		if (!systemUuid || !this.systems)
			return null;

		var sys : System[] = $.grep(this.systems, function(s : System) { return s.uuid === systemUuid;});

		if (sys.length > 0)
			return sys[0];
		else
			return null;
	}

	private getTechnicalInterface(technicalInterfaceUuid : string) : TechnicalInterface {
		if (!technicalInterfaceUuid || !this.technicalInterfaces)
			return null;

		var ti : TechnicalInterface[] = $.grep(this.technicalInterfaces, function(ti : TechnicalInterface) { return ti.uuid === technicalInterfaceUuid;});

		if (ti.length > 0)
			return ti[0];
		else
			return null;
	}

	private editOrganisations() {
		var vm = this;
		OrganisationPickerDialog.open(vm.$modal, vm.organisations)
			.result.then(function (result : Organisation[]) {
			vm.organisations = result;
		});
	}

	loadSystems() {
		var vm = this;
		vm.libraryService.getSystems()
			.subscribe(
				(result) => {
				vm.systems = result;
				vm.technicalInterfaces = [];
				console.log(vm.systems[0].technicalInterface.length);
				console.log(vm.systems[0].technicalInterface[0].name);

				for (var i = 0; i < vm.systems.length; ++i) {
					for (var j = 0; j < vm.systems[i].technicalInterface.length; ++j) {
						var technicalInterface = {
							uuid: vm.systems[i].technicalInterface[j].uuid,
							name: vm.systems[i].technicalInterface[j].name,
							messageType: vm.systems[i].technicalInterface[j].messageType,
							messageFormat: vm.systems[i].technicalInterface[j].messageFormat,
							messageFormatVersion: vm.systems[i].technicalInterface[j].messageFormatVersion
						} as TechnicalInterface;
						vm.technicalInterfaces.push(technicalInterface);
					}
				}
			},
				(error) => {
				vm.log.error('Failed to load systems', error, 'Load systems');
				MessageBoxDialog.open(vm.$modal, 'Load systems', 'Failed to load Systems.  Ensure Systems are configured in the protocol manager', 'OK', null);
			});
	}
}
