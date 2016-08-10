/// <reference path="../../typings/index.d.ts" />
/// <reference path="../core/logging.service.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.logging {
	import ILoggingService = app.core.ILoggingService;
	import LoggingEvent = app.models.LoggingEvent;
	import Service = app.models.Service;
	import IServiceService = app.service.IServiceService;

	'use strict';

	export class LoggingController {
		loggingEvents:LoggingEvent[];
		services : Service[];
		serviceId : string;

		static $inject = ['LoggingService', 'LoggerService', 'ServiceService', '$state'];

		constructor(protected loggingService:ILoggingService,
					protected logger:ILoggerService,
					protected serviceService : IServiceService,
					protected $state : IStateService) {
			this.loadServices();
			this.refresh();
		}

		refresh() {
			var vm = this;
			var serviceName = $("#service>option:selected").html()
			this.getLoggingEvents(vm.serviceId);
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

		getLoggingEvents(serviceId : string) {
			var vm = this;
			vm.loggingEvents = null;
			vm.loggingService.getLoggingEvents(serviceId)
				.then(function (data:LoggingEvent[]) {
					vm.loggingEvents = data;
				});
		}

		actionItem(event : LoggingEvent, action : string) {
			alert(action+" : "+event.loggerName);
		}
	}

	angular
		.module('app.logging')
		.controller('LoggingController', LoggingController);
}
