import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService, Transition} from "ui-router-ng2";
import {Service} from "./models/Service";
import {System} from "../system/models/System";
import {TechnicalInterface} from "../system/models/TechnicalInterface";
import {Endpoint} from "./models/Endpoint";
import {ServiceService} from "./service.service";
import {AdminService, LoggerService, MessageBoxDialog} from "eds-common-js";
import {SystemService} from "../system/system.service";
import {EdsLibraryItem} from "../edsLibrary/models/EdsLibraryItem";
import {OdsSearchDialog} from "./odsSearch.dialog";
import {OrganisationType} from "./models/OrganisationType";

@Component({
	template : require('./serviceEditor.html')
})
export class ServiceEditComponent {

	service : Service = <Service>{};
	systems : System[];
	technicalInterfaces : TechnicalInterface[];
	protocols: EdsLibraryItem[];
	//protocolJson: string;

	selectedEndpoint : Endpoint;


	//keep this here so it only needs getting once
	organisationTypes: OrganisationType[];

	constructor(private $modal : NgbModal,
							private $window : StateService,
							private log:LoggerService,
							private adminService : AdminService,
							private serviceService : ServiceService,
							private systemService : SystemService,
							private transition : Transition) {

		var vm = this;
		vm.loadOrganisationTypes();
		vm.loadSystems();

		vm.performAction(transition.params()['itemAction'], transition.params()['itemUuid']);
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
					vm.getServiceProtocols();
				},
				(error) => vm.log.error('Error loading', error, 'Error')
			);
	}

	save(close : boolean) {
		var vm = this;

		//doesn't immediately save - calls validation function first which checks the save is safe
		vm.serviceService.validateSave(vm.service)
			.subscribe(
				(result) => {

					//if we have a message then log
					if (result) {
						vm.log.warning(result);
					} else {
						vm.reallySave(close);
					}
				},
				(error) => {
					vm.log.error('Error validating', error, 'Error');
				}
			);
	}

	private reallySave(close: boolean) {
		var vm = this;

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

	private addEndpoint(publisher: boolean) {
		var newEndpoint = {} as Endpoint;
		if (publisher) {
			newEndpoint.endpoint = 'Publisher_Bulk';
		} else {
			newEndpoint.endpoint = '';
		}
		this.service.endpoints.push(newEndpoint);
		this.selectedEndpoint = newEndpoint;
	}

	removeEndpoint(index: number, scope : any) {
		this.service.endpoints.splice(index, 1);
		if (this.selectedEndpoint === scope.item) {
			this.selectedEndpoint = null;
		}
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

	private getInterfaceTypeLetter(o: Endpoint): string {
		var vm = this;

		if (!o) {
			return null;
		} else if (vm.isPublisher(o)) {
			return 'Publisher';
		} else {
			return 'Subscriber';
		}
	}

	private isPublisher(o: Endpoint): boolean {
		return o.endpoint.startsWith('Publisher_');
	}

	private loadOrganisationTypes() {
		var vm = this;

		//if already done, return
		if (vm.organisationTypes) {
			return;
		}

		var vm = this;
		vm.serviceService.getOrganisationTypeList()
			.subscribe(
				(result) => {
					vm.organisationTypes = result;
				},
				(error) => {
					vm.log.error('Failed to retrieve organisation type list');
				}
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

	deleteService() {
		var vm = this;
		MessageBoxDialog.open(vm.$modal, 'Delete Service', 'Are you sure you want to delete the Service?', 'Yes', 'No')
			.result.then(
			() => vm.doDeleteService(vm.service),
			() => vm.log.info('Delete cancelled')
		);
	}

	private doDeleteService(item : Service) {
		var vm = this;
		vm.serviceService.delete(item.uuid)
			.subscribe(
				() => {
					vm.log.success('Service deleted', item, 'Delete Service');
					vm.close();
				},
				(error) => vm.log.error('Failed to delete Service', error, 'Delete Service')
			);
	}


	deleteData() {
		var vm = this;

		MessageBoxDialog.open(vm.$modal, 'Delete Data', 'Are you sure you want to delete all data for this Service?', 'Yes', 'No')
			.result.then(
			() => vm.doDeleteData(vm.service),
			() => vm.log.info('Delete data cancelled')
		);

	}


	private doDeleteData(service: Service) {
		var vm = this;
		vm.serviceService.deleteData(service.uuid)
			.subscribe(
				() => {
					vm.log.success('Data deletion queued up', service, 'Delete Data');
					vm.close();
				},
				(error) => vm.log.error('Failed to delete data', error, 'Delete Data')
			);
	}

	private getServiceProtocols() {
		var vm = this;
		vm.serviceService.getServiceProtocols(vm.service.uuid)
			.subscribe(
				(result) => {
					vm.protocols = result;
					//vm.protocolJson = JSON.stringify(result, null, 2);
				},
				(error) => vm.log.error('Failed to load service protocols', error, 'Load service protocols')
			);
	}

	odsSearch() {
		var vm = this;
		OdsSearchDialog.open(vm.$modal);
	}

	autoFillDetailsFromOds() {
		var vm = this;
		var odsCode = vm.service.localIdentifier;
		if (!odsCode) {
			vm.log.error('No ODS code entered');
			return;
		}

		vm.serviceService.getOpenOdsRecord(vm.service.localIdentifier)
			.subscribe(
				(result) => {

					//validate we got something
					if (!result['organisationName']) {
						vm.log.error('No ODS record found for ' + odsCode);
						return;
					}

					var newName = result['organisationName'];
					if (newName) {
						vm.service.name = newName;
					} else {
						vm.service.name = '';
					}

					var newPostcode = result['postcode'];
					if (newPostcode) {
						vm.service.postcode = newPostcode;
					} else {
						vm.service.postcode = '';
					}

					//ccg
					var newParents = result['parents'];
					if (newParents) {
						var parentKeys = Object.keys(newParents);

						vm.service.ccgCode = '';

						if (parentKeys.length == 1) {
							var parentKey = parentKeys[0];
							vm.service.ccgCode = parentKey;

						} else {

							for (var i = 0; i < parentKeys.length; i++) {
								var parentKey = parentKeys[i];
								var parentVal = newParents[parentKey] as string;
								parentVal = parentVal.toUpperCase();

								//some orgs have multiple parents which seems to work OK if we ignore the old
								//SHA parents and GENOMIC ones
								if (parentVal.indexOf('STRATEGIC HEALTH AUTHORITY') == -1
									&& parentVal.indexOf('GENOMIC') == -1) {

									vm.service.ccgCode = parentKey;
								}
							}
						}
					}

					//org type
					var orgTypeName = result['organisationType'];
					if (orgTypeName) {

						vm.service.organisationTypeCode = '';
						vm.service.organisationTypeDesc = '';

						for (var i = 0; i < vm.organisationTypes.length; i++) {
							var organisationType = vm.organisationTypes[i];
							var name = organisationType.name;

							//the org type in the ODS results is the NAME of the OrganisationType enum, not the code or desc
							if (name == orgTypeName) {
								vm.service.organisationTypeCode = organisationType.code;
								vm.service.organisationTypeDesc = organisationType.description;
								break;
							}
						}

					}

				},
				(error) => {
					vm.log.error('Failed to find ODS record');
				}
			);
	}
}
