/// <reference path="../../typings/index.d.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.audit {
	import IAuditService = app.core.IAuditService;
	import AuditEvent = app.models.AuditEvent;
	import User = app.models.User;
	import OrganisationPickerController = app.organisation.OrganisationPickerController;
	import Organisation = app.models.Organisation;
	import IOrganisationService = app.organisation.IOrganisationService;


	'use strict';

	export class AuditController {
		module : string;
		user : User;
		month : Date;
		organisation : Organisation;
		submodule : string;
		action : string;

		modules : string[];
		users : User[];
		organisations : Organisation[];
		submodules : string[];
		actions : string[];
		auditEvents:AuditEvent[];

		static $inject = ['$scope', 'AuditService', 'LoggerService', 'OrganisationService', '$uibModal'];

		constructor(
					protected $scope : any,
					protected auditService:IAuditService,
					protected logger:ILoggerService,
					protected organisationService : IOrganisationService,
					protected $modal : IModalService) {

			this.month = new Date();
			this.month.setDate(1);
			this.loadModules();
			this.loadUsers();
			this.loadOrganisations();
			this.loadActions();
			this.refresh();
		}

		loadUsers() {
			var vm = this;
			vm.users = null;
			vm.auditService.getUsers()
				.then(function(data:User[]) {
					vm.users = data;
				});
		}

		loadOrganisations() {
			var vm = this;
			vm.organisations = null;
			vm.organisationService.getOrganisations()
				.then(function(data:Organisation[]) {
					vm.organisations = data;
				});
		}

		loadModules() {
			var vm = this;
			vm.modules = null;
			vm.auditService.getModules()
				.then(function (data:string[]) {
					vm.modules = data;
				});
		}

		loadSubmodules() {
			var vm = this;
			vm.submodules = null;
			vm.auditService.getSubmodules(vm.module)
				.then(function (data:string[]) {
					vm.submodules = data;
				});
		}

		loadActions() {
			var vm = this;
			vm.actions = null;
			vm.auditService.getActions()
				.then(function (data:string[]) {
					vm.actions = data;
				});
		}

		refresh() {
			var vm = this;

			vm.auditEvents = [];

			if (!vm.user || !vm.module || !vm.month)
				return;

			vm.loadSubmodules();

			if (vm.submodule == '') vm.submodule = null;
			if (vm.action == '') vm.action = null;

			vm.getAuditEvents();
		}

		getAuditEvents() {
			var vm = this;
			vm.auditEvents = null;

			var organisationId : string = null;
			if (vm.organisation)
				organisationId = vm.organisation.uuid;
				vm.auditService.getUserAudit(
					vm.module,
					vm.user.uuid,
					vm.month,
					organisationId
				)
				.then(function (data:AuditEvent[]) {
					vm.auditEvents = data;
				});
		}

		getFilteredEvents(vm : any) {
			return vm.auditEvents.filter(
				function(item : any) {
					if (!vm.submodule || vm.submodule === '')
						return true;
					if (item.subModule === vm.submodule) {
						if (!vm.action || vm.action === '')
							return true;
						if (item.action === vm.action)
							return true;
					}

					return false;
				}
			);
		}
	}


	angular
		.module('app.audit')
		.controller('AuditController', AuditController);
}
