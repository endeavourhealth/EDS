import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService, Transition} from "ui-router-ng2";
import {Service} from "./models/Service";
import {Organisation} from "../organisations/models/Organisation";
import {System} from "../system/models/System";
import {TechnicalInterface} from "../system/models/TechnicalInterface";
import {Endpoint} from "./models/Endpoint";
import {ServiceService} from "./service.service";
import {OrganisationPickerDialog} from "../organisations/organisationPicker.dialog";
import {AdminService, LoggerService, MessageBoxDialog} from "eds-common-js";
import {SystemService} from "../system/system.service";
import {OrganisationService} from "../organisations/organisation.service";

@Component({
	template : require('./serviceEditor.html')
})
export class ServiceEditComponent {

	service : Service = <Service>{};
	organisations : Organisation[];
	systems : System[];
	technicalInterfaces : TechnicalInterface[];

	selectedEndpoint : Endpoint;

	constructor(private $modal : NgbModal,
							private $window : StateService,
							private log:LoggerService,
							private adminService : AdminService,
							private serviceService : ServiceService,
							private systemService : SystemService,
							private organisationService : OrganisationService,
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
			name : '',
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

	removeEndpoint(index: number, scope : any) {
		this.service.endpoints.splice(index, 1);
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

	/**
	 * attempts to automatically match to an existing organisation or creates one otherwise
	 */
	private autoSetOrganisation() {

		//console.log('auto org');
		//ensure got an ODS code
		var vm = this;

		if (vm.organisations.length > 0) {
			vm.log.error('Organisation already set');
			return;
		}

		var odsCode = vm.service.localIdentifier;
		if (!odsCode) {
			vm.log.error('Need national ID');
			return;
		}
		var name = vm.service.name;
		if (!name) {
			vm.log.error('Need name');
			return;
		}

		//search for org based on ODS code
		//console.log('doing search for ' + odsCode);
		vm.organisationService.search(odsCode)
			.subscribe(
				(result) => {
					if (result.length == 0) {
						//create new one
						//console.log('no match found so will create');
						vm.createOrganisation(odsCode, name);

					} else if (result.length == 1) {
						//use this one
						//console.log('matched to one org');
						vm.organisations = [];
						vm.organisations.push(result[0]);

					} else {
						vm.log.error('More than one organisation found for ' + odsCode);
					}
				},
				(error) => vm.log.error(error)
			);
	}

	private createOrganisation(odsCode: string, name: string) {

		var vm = this;

		var organisation = <Organisation>{};
		organisation.name = name;
		organisation.nationalId = odsCode;
		organisation.services = {};

		//console.log('saving new org');
		vm.organisationService.saveOrganisation(organisation)
			.subscribe(saved => {
					//console.log('saved OK so will add to orgs with UUID ' + saved.uuid);
					vm.organisations = [];
					vm.organisations.push(saved);
				},
				error => vm.log.error('Error saving', error, 'Error')
			);
	}

	loadSystems() {
		var vm = this;
		vm.systemService.getSystems()
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
