import IModalService = angular.ui.bootstrap.IModalService;
import IWindowService = angular.IWindowService;

import {Service} from "../../models/Service";
import {Organisation} from "../../models/Organisation";
import {System} from "../../models/System";
import {TechnicalInterface} from "../../models/TechnicalInterface";
import {Endpoint} from "../../models/Endpoint";
import {IAdminService} from "../../core/admin.service";
import {ILibraryService} from "../../core/library.service";
import {IServiceService} from "../service/service.service";
import {OrganisationPickerController} from "../../organisations/picker/organisationPicker.controller";
import {MessageBoxController} from "../../dialogs/messageBox/messageBox.controller";
import {ILoggerService} from "../../blocks/logger.service";

export class ServiceEditorController {
	static $inject = ['$uibModal', '$window', 'LoggerService', 'AdminService', 'LibraryService', 'ServiceService', '$stateParams'];

	service : Service;
	organisations : Organisation[];
	systems : System[];
	technicalInterfaces : TechnicalInterface[];

	selectedEndpoint : Endpoint;

	constructor(private $modal : IModalService,
							private $window : IWindowService,
							private log:ILoggerService,
							private adminService : IAdminService,
							private libraryService : ILibraryService,
							private serviceService : IServiceService,
							private $stateParams : {itemAction : string, itemUuid : string}) {

		this.loadSystems();
		this.performAction($stateParams.itemAction, $stateParams.itemUuid);
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
			.then(function(result : Service) {
				vm.service = result;
				vm.getServiceOrganisations();
			})
			.catch(function(data) {
				vm.log.error('Error loading', data, 'Error');
			});
	}

	save(close : boolean) {
		var vm = this;

		// Populate service organisations before save
		vm.service.organisations = {};
		for (var idx in this.organisations) {
			var organisation : Organisation = this.organisations[idx];
			this.service.organisations[organisation.uuid] = organisation.name;
		}

		vm.serviceService.save(vm.service)
			.then(function(saved : Service) {
				vm.service.uuid = saved.uuid;
				vm.adminService.clearPendingChanges();
				vm.log.success('Item saved', vm.service, 'Saved');
				if (close) { vm.$window.history.back(); }
			})
			.catch(function(data : any) {
				vm.log.error('Error saving', data, 'Error');
			});
	}

	close() {
		this.adminService.clearPendingChanges();
		this.$window.history.back();
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
			.then(function(result : Organisation[]) {
				vm.organisations = result;
			})
			.catch(function (error : any) {
				vm.log.error('Failed to load service organisations', error, 'Load service organisations');
			});
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
		OrganisationPickerController.open(vm.$modal, vm.organisations)
			.result.then(function (result : Organisation[]) {
			vm.organisations = result;
		});
	}

	loadSystems() {
		var vm = this;
		vm.libraryService.getSystems()
			.then(function(result) {
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
			})
			.catch(function (error) {
				vm.log.error('Failed to load systems', error, 'Load systems');
				MessageBoxController.open(vm.$modal,
					'Load systems', 'Failed to load Systems.  Ensure Systems are configured in the protocol manager', 'OK', null);
			});
	}
}
