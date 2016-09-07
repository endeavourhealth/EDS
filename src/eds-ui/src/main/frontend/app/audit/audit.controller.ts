/// <reference path="../../typings/index.d.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.audit {
	import IAuditService = app.core.IAuditService;
	import AuditEvent = app.models.AuditEvent;
	import User = app.models.User;
	import OrganisationPickerController = app.organisation.OrganisationPickerController;
	import Organisation = app.models.Organisation;

	'use strict';

	export class AuditController {
		userId : string = 'fbb470ed-64fb-4c50-9564-b15065d462e7';
		user : string = 'Professional User';
		organisationId : string;
		organisationName : string = '';
		module : string;
		submodule : string;
		action : string;

		modules : string[];
		submodules : string[];
		actions : string[];
		auditEvents:AuditEvent[];

		static $inject = ['AuditService', 'LoggerService', '$uibModal'];

		constructor(protected auditService:IAuditService,
					protected logger:ILoggerService,
					protected $modal : IModalService) {
			this.loadModules();
			this.loadActions();
			this.refresh();
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
			vm.auditService.getAuditEvents(
				vm.userId,
				vm.organisationId,
				vm.module,
				vm.submodule,
				vm.action
			)
				.then(function (data:AuditEvent[]) {
					vm.auditEvents = data;
				});
		}

		pickOrganisation() {
			var vm = this;
			OrganisationPickerController.open(vm.$modal, [])
				.result.then(function (result : Organisation[]) {
				vm.organisationId = result[0].uuid;
				vm.organisationName = result[0].name;
				vm.refresh();
			});
		}
	}


	angular
		.module('app.audit')
		.controller('AuditController', AuditController);
}
