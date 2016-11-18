import {LoggingEvent} from "../models/LoggingEvent";
import {Service} from "../models/Service";
import {LoggingService} from "./logging.service";
import {LoggerService} from "../common/logger.service";
import {LogEntryDialog} from "./logEntry.dialog";
import {ServiceService} from "../services/service.service";
import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
	template : require('./logging.html')
})
export class LoggingComponent {
	loggingEvents:LoggingEvent[];
	services : Service[];
	serviceId : string;
	level : string;
	page : number;

	constructor(protected loggingService:LoggingService,
				protected logger:LoggerService,
				protected serviceService : ServiceService,
				protected $modal : NgbModal) {
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
			.subscribe(
				(result) => vm.services = result,
				(error) => vm.logger.error('Failed to load services', error, 'Load services')
			);
	}

	getLoggingEvents(serviceId : string, level : string, page : number) {
		var vm = this;
		vm.loggingEvents = null;
		vm.loggingService.getLoggingEvents(serviceId, level, page)
			.subscribe(
				(data) => vm.loggingEvents = data,
				(error) => vm.logger.error('Failed to load events', error, 'Load events')
			);
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
			.subscribe(
				(data) => vm.showStackTrace(event, data),
				(error) => vm.logger.error('Failed to load stack trace', error, 'Load stack trace')
			);
	}

	showStackTrace(event : LoggingEvent, stackTrace : string) {
		var vm = this;
		LogEntryDialog.open(vm.$modal, event, stackTrace);
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
