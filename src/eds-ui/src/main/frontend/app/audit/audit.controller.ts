/// <reference path="../../typings/index.d.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.audit {
	import IAuditService = app.core.IAuditService;
	import AuditEvent = app.models.AuditEvent;

	'use strict';

	export class AuditController {
		serviceId : string;
		auditEvents:AuditEvent[];

		static $inject = ['AuditService', 'LoggerService', '$uibModal'];

		constructor(protected auditService:IAuditService,
					protected logger:ILoggerService,
					protected $modal : IModalService) {
			this.refresh();
		}

		refresh() {
			var vm = this;
			this.getAuditEvents(vm.serviceId);
		}

		getAuditEvents(serviceId : string) {
			var vm = this;
			vm.auditEvents = null;
			vm.auditService.getAuditEvents(serviceId)
				.then(function (data:AuditEvent[]) {
					vm.auditEvents = data;
				});
		}
	}


	angular
		.module('app.audit')
		.controller('AuditController', AuditController);
}
