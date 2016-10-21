import IStateService = angular.ui.IStateService;

import {TransformErrorSummary} from "../models/TransformErrorSummary";
import {TransformErrorDetail} from "../models/TransformErrorDetail";
import {Service} from "../models/Service";
import {System} from "../models/System";
import {ITransformErrorsService} from "../core/transformErrors.service";
import {ILoggerService} from "../blocks/logger.service";
import {IServiceService} from "../services/service/service.service";

export class TransformErrorsController {
	transformErrorSummaries:TransformErrorSummary[];
	transformErrorDetail:TransformErrorDetail;

	services : Service[];
	serviceId : string;
	systems : System[];
	systemId : string;
	localId : string;
	nhsNumber : string;
	patientId : string;

	static $inject = ['TransformErrorsService', 'LoggerService', 'ServiceService', '$state'];

	constructor(protected transformErrorService:ITransformErrorsService,
				protected logger:ILoggerService,
				protected serviceService : IServiceService,
				protected $state : IStateService) {
		this.loadServices();
		this.refresh();
	}

	refresh() {
		/*var vm = this;
		 var serviceName = $("#service>option:selected").html()
		 this.getPatientIdentityEvents(vm.serviceId);*/
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

	loadSystems() {
		var vm = this;
		vm.serviceService.getSystemsForService(vm.serviceId)
			.then(function(result) {
				vm.systems = result;
			})
			.catch(function (error) {
				vm.logger.error('Failed to load systems', error, 'Load systems');
			});
	}

	getPatientIdentitiesByPatientId() {
		/*var vm = this;
		 vm.patientIdentities = null;
		 vm.patientIdentityService.getByPatientId(vm.patientId)
		 .then(function (data:PatientIdentity[]) {
		 vm.patientIdentities = data;
		 if (data.length == 0) {
		 vm.logger.error('No results found');
		 }
		 });*/
	}

	getPatientIdentitiedByNhsNumber() {
		/*var vm = this;
		 vm.patientIdentities = null;
		 vm.patientIdentityService.getByNhsNumber(vm.nhsNumber)
		 .then(function (data:PatientIdentity[]) {
		 vm.patientIdentities = data;
		 if (data.length == 0) {
		 vm.logger.error('No results found');
		 }
		 });*/
	}

	getPatientIdentitiesByLocalIdentifier() {
		/*var vm = this;
		 vm.patientIdentities = null;
		 vm.patientIdentityService.getByLocalIdentifier(vm.serviceId, vm.systemId, vm.localId)
		 .then(function (data:PatientIdentity[]) {
		 vm.patientIdentities = data;
		 if (data.length == 0) {
		 vm.logger.error('No results found');
		 }
		 });*/
	}

	/*actionItem(event : PatientIdentityEvent, action : string) {
	 alert(action+" : "+event.loggerName);
	 }*/

	actionItem(uuid : string, action : string) {
		this.$state.go('app.resources', {itemUuid: uuid, itemAction: action});
	}
}
