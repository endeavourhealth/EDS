/// <reference path="../../typings/index.d.ts" />
/// <reference path="../core/logging.service.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.logging {
	import ILoggingService = app.core.ILoggingService;
	import LoggingEvent = app.models.LoggingEvent;
	import Service = app.models.Service;
	import IServiceService = app.service.IServiceService;
	import MessageBoxController = app.dialogs.MessageBoxController;

	'use strict';

	export class LoggingController {
		loggingEvents:LoggingEvent[];
		services : Service[];
		serviceId : string;
		level : string;
		page : number;

		static $inject = ['LoggingService', 'LoggerService', 'ServiceService', '$uibModal'];

		constructor(protected loggingService:ILoggingService,
					protected logger:ILoggerService,
					protected serviceService : IServiceService,
					protected $modal : IModalService) {
			this.page = 0;
			this.loadServices();
			this.refresh();
		}

		refresh() {
			var vm = this;
			this.getLoggingEvents(vm.serviceId, vm.level, vm.page);
		}

		loadServices() {
			var vm = this;
			vm.serviceService.getAll()
				.then(function(result) {
					vm.services = result;
				})
				.catch(function (error) {
					vm.logger.error('Failed to load services', error, 'Load services');
				});
		}

		getLoggingEvents(serviceId : string, level : string, page : number) {
			var vm = this;
			vm.loggingEvents = null;
			vm.loggingService.getLoggingEvents(serviceId, level, page)
				.then(function (data:LoggingEvent[]) {
					vm.loggingEvents = data;
				});
		}

		first() {
			var vm = this;
			if (vm.page > 0) {
				vm.page = 0;
				vm.getLoggingEvents(vm.serviceId, vm.level, vm.page);
			}
		}

		previous() {
			var vm = this;
			if (vm.page > 0) {
				vm.page--;
				vm.getLoggingEvents(vm.serviceId, vm.level, vm.page);
			}
		}

		next() {
			var vm = this;
			vm.page++;
			vm.getLoggingEvents(vm.serviceId, vm.level, vm.page);
		}

		actionItem(event : LoggingEvent, action : string) {
			var vm = this;
			vm.loggingService.getStackTrace(event.eventId)
				.then(function (data:string) {
					vm.showStackTrace(event, data);
				});
		}

		showStackTrace(event : LoggingEvent, stackTrace : string) {
			var vm = this;
			MessageBoxController.openLarge(
				vm.$modal,
				event.formattedMessage + ' in ' + event.callerMethod,
				stackTrace,
				null,
				'Close'
			);
		}
	}

	angular
		.module('app.logging')
		.controller('LoggingController', LoggingController);
}
