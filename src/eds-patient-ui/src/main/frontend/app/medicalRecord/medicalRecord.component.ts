import {StateService} from "angular-ui-router";

import {ILoggerService} from "../blocks/logger.service";
import {PatientService} from "../models/PatientService";
import {MedicalRecordService} from "./medicalRecord.service";

export class MedicalRecordComponent implements ng.IComponentOptions {
	template : string;
	controller : string;
	controllerAs : string;

	constructor () {
		this.template = require('./medicalRecord.html');
		this.controller = 'MedicalRecordController';
		this.controllerAs = '$ctrl';
	}
}

export class MedicalRecordController {
	static $inject = ['MedicalRecordService', 'LoggerService', '$state'];

	services : PatientService[];
	selectedService : string;

	constructor(private medicalRecordService:MedicalRecordService,
							private logger:ILoggerService,
							private $state : StateService) {
		this.loadServiceList();
	}

	loadServiceList() {
		var vm = this;
		vm.services = null;
		vm.medicalRecordService.getServices()
			.then(function(data : PatientService[])
				{
					vm.services = data;
				}
			);
	}
}
