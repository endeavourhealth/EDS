import {PatientService} from "../models/PatientService";
import {MedicalRecordService} from "./medicalRecord.service";
import {Component} from "@angular/core";
import 'rxjs/add/operator/toPromise';
import {EdsLoggerService} from "../blocks/logger.service";

@Component({
	selector: 'medical-record-component',
	template: require('./medicalRecord.html')
})
export class MedicalRecordComponent {
	services : PatientService[];
	selectedService : string = "";

	constructor(private medicalRecordService:MedicalRecordService, private log : EdsLoggerService) {
		this.loadServiceList();
	}

	loadServiceList() {
		this.log.info("Loading services");
		this.medicalRecordService.getServices()
			.subscribe((services : PatientService[]) => {
				this.services = services;
				this.log.info("Services loaded");
			});
	}

	refresh() {
		this.log.info("Selected Service : " + this.selectedService);
	}
}
