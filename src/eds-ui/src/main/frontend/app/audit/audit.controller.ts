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
		user : User;
		organisation : Organisation;
		module : string;
		submodule : string;
		action : string;

		users : User[];
		organisations : Organisation[];
		modules : string[];
		submodules : string[];
		actions : string[];
		auditEvents:AuditEvent[];

		static $inject = ['AuditService', 'LoggerService', 'OrganisationService', '$uibModal'];

		constructor(protected auditService:IAuditService,
					protected logger:ILoggerService,
					protected organisationService : IOrganisationService,
					protected $modal : IModalService) {

			this.loadUsers();
			this.loadOrganisations();
			this.loadModules();
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

			if (!vm.user)
				return;

			if (vm.module == '')
				vm.module = null;
			else
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
			vm.auditService.getAuditEvents(
				vm.user.uuid,
				organisationId,
				vm.module,
				vm.submodule,
				vm.action
			)
				.then(function (data:AuditEvent[]) {
					vm.auditEvents = data;
				});
		}

		//pickUser() {
		//	UserPickerController.open(vm.$modal, vm.userId)
		//}
		//
		//pickOrganisation() {
		//	var vm = this;
		//	OrganisationPickerController.open(vm.$modal, [vm.organisation])
		//		.result.then(function (result : Organisation[]) {
		//		vm.organisation = result[0];
		//		vm.refresh();
		//	});
		//}
	}


	angular
		.module('app.audit')
		.controller('AuditController', AuditController);
}
