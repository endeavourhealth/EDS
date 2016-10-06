import IModalService = angular.ui.bootstrap.IModalService;
import IWindowService = angular.IWindowService;

import {Organisation} from "../../models/Organisation";
import {Service} from "../../models/Service";
import {IAdminService} from "../../core/admin.service";
import {IOrganisationService} from "../service/organisation.service";
import {ILoggerService} from "../../blocks/logger.service";
import {ServicePickerController} from "../../services/picker/servicePicker.controller";

export class OrganisationEditorController {
	static $inject = ['$uibModal', '$window', 'LoggerService', 'AdminService', 'OrganisationService', '$stateParams'];

	organisation : Organisation;
	services : Service[];

	constructor(private $modal : IModalService,
							private $window : IWindowService,
							private log:ILoggerService,
							private adminService : IAdminService,
							private organisationService : IOrganisationService,
							private $stateParams : {itemAction : string, itemUuid : string}) {
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
		this.organisation = {
			uuid : uuid,
			name : 'New item'
		} as Organisation;
	}

	load(uuid : string) {
		var vm = this;
		vm.organisationService.getOrganisation(uuid)
			.then(function(result : Organisation) {
				vm.organisation = result;
				vm.getOrganisationServices();
			})
			.catch(function(data) {
				vm.log.error('Error loading', data, 'Error');
			});
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
			.then(function(saved : Service) {
				vm.organisation.uuid = saved.uuid;
				vm.adminService.clearPendingChanges();
				vm.log.success('Item saved', vm.organisation, 'Saved');
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

	private getOrganisationServices() {
		var vm = this;
		vm.organisationService.getOrganisationServices(vm.organisation.uuid)
			.then(function(result : Service[]) {
				vm.services = result;
			})
			.catch(function (error : any) {
				vm.log.error('Failed to load organisation services', error, 'Load organisation services');
			});
	}

	private editServices() {
		var vm = this;
		ServicePickerController.open(vm.$modal, vm.services)
			.result.then(function (result : Service[]) {
				vm.services = result;
		});
	}
}
