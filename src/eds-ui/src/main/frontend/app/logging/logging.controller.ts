import IModalService = angular.ui.bootstrap.IModalService;

import {LoggingEvent} from "../models/LoggingEvent";
import {Service} from "../models/Service";
import {ILoggingService} from "../core/logging.service";
import {ILoggerService} from "../blocks/logger.service";
import {LogEntryDialogController} from "./logEntryDialog.controller";
import {IServiceService} from "../services/service/service.service";

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
		this.loadServices();
		this.refresh();
	}

	refresh() {
		var vm = this;
		this.page = 0;
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
		LogEntryDialogController.open(vm.$modal, event, stackTrace);
	}

	getLevelIcon(level : string) {
		switch(level) {
			case "TRACE" :
				return "fa fa-fw fa-search text-success";
			case "DEBUG":
				return "fa fa-fw fa-bug text-primary";
			case "INFO":
				return "fa fa-fw fa-info text-info";
			case "WARN" :
				return "fa fa-fw fa-exclamation-circle text-warning";
			case "ERROR":
				return "fa fa-fw fa-ban text-danger";
			case "FATAL":
				return "fa fa-fw fa-stop text-danger";
			default:
				return "fa fa-fw fa-space";
		}
	}
}
