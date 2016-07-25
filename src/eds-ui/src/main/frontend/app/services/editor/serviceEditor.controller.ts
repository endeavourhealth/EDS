/// <reference path="../../../typings/index.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />

module app.service {
	import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
	import IModalSettings = angular.ui.bootstrap.IModalSettings;
	import IModalService = angular.ui.bootstrap.IModalService;
	import IWindowService = angular.IWindowService;
	import BaseDialogController = app.dialogs.BaseDialogController;
	import Service = app.models.Service;
	import Organisation = app.models.Organisation;
	import OrganisationPickerController = app.organisation.OrganisationPickerController;
	import Endpoint = app.models.Endpoint;
	import ILibraryService = app.core.ILibraryService;
	import System = app.models.System;
	import TechnicalInterface = app.models.TechnicalInterface;

	'use strict';

	export class ServiceEditorController {
		static $inject = ['$uibModal', '$window', 'LoggerService', 'AdminService', 'LibraryService', 'ServiceService', '$stateParams'];

		service : Service;
		organisations : Organisation[];
		systems : System[];
		technicalInterfaces : TechnicalInterface[];

		selectedEndpoint : Endpoint;

		constructor(private $modal : IModalService,
								private $window : IWindowService,
								private log:app.blocks.ILoggerService,
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
				});
		}
	}

	angular
		.module('app.service')
		.controller('ServiceEditorController', ServiceEditorController);
}
