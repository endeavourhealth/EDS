/// <reference path="../../typings/index.d.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.audit {
	import IAuditService = app.core.IAuditService;
	import AuditEvent = app.models.AuditEvent;

	'use strict';

	export class AuditController {
		userId : string;
		serviceId : string;
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
				'fbb470ed-64fb-4c50-9564-b15065d462e7',
				vm.serviceId,
				vm.module,
				vm.submodule,
				vm.action
			)
				.then(function (data:AuditEvent[]) {
					vm.auditEvents = data;
				});
		}
	}


	angular
		.module('app.audit')
		.controller('AuditController', AuditController);
}
