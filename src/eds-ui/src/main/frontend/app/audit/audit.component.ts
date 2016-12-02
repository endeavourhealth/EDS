import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {LoggerService} from "../common/logger.service";
import {OrganisationService} from "../organisations/organisation.service";
import {AuditService} from "./audit.service";
import {User} from "../users/models/User";
import {Organisation} from "../organisations/models/Organisation";
import {AuditEvent} from "./models/AuditEvent";

@Component({
	template : require('./audit.html')
})
export class AuditComponent {
	module : string = "";
	user : string = "";
	month : Date;
	organisation : string = "";
	submodule : string = "";
	action : string = "";

	modules : string[];
	users : User[];
	organisations : Organisation[];
	submodules : string[];
	actions : string[];
	auditEvents:AuditEvent[];


	constructor(
				protected auditService:AuditService,
				protected log:LoggerService,
				protected organisationService : OrganisationService,
				protected $modal : NgbModal) {

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
			.subscribe(
				(data) => vm.users = data,
				(error) => vm.log.error("Error loading users", error, "Error")
			);
	}

	loadOrganisations() {
		var vm = this;
		vm.organisations = null;
		vm.organisationService.getOrganisations()
			.subscribe(
				(data) => vm.organisations = data,
				(error) => vm.log.error("Error loading organisations", error, "Error")
			);
	}

	loadModules() {
		var vm = this;
		vm.modules = null;
		vm.auditService.getModules()
			.subscribe(
				(data) => vm.modules = data,
				(error) => vm.log.error("Error loading modules", error, "Error")
			);
	}

	loadSubmodules() {
		var vm = this;
		vm.submodules = null;
		vm.auditService.getSubmodules(vm.module)
			.subscribe(
				(data) => vm.submodules = data,
				(error) => vm.log.error("Error loading sub modules", error, "Error")
			);
	}

	loadActions() {
		var vm = this;
		vm.actions = null;
		vm.auditService.getActions()
			.subscribe(
				(data) => vm.actions = data,
				(error) => vm.log.error("Error loading actions", error, "Error")
			);
	}

	refresh() {
		var vm = this;

		vm.auditEvents = [];

		if (!vm.user || !vm.module || !vm.month)
			return;

		vm.loadSubmodules();
		vm.getAuditEvents();
	}

	getAuditEvents() {
		var vm = this;
		vm.auditEvents = null;

		var organisationId: string = null;
		if (vm.organisation)
			organisationId = vm.organisation;

		vm.auditService.getUserAudit(vm.module, vm.user, vm.month, organisationId)
			.subscribe(
				(data) => vm.auditEvents = data,
				(error) => vm.log.error("Error loading audit events", error, "Error")
			);
	}

	getFilteredEvents(vm : any) {
		if (!vm.auditEvents)
			return null;

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
