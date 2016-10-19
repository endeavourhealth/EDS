import {PatientService} from "../models/PatientService";
import {MedicalRecordService} from "./medicalRecord.service";
import {Component} from "@angular/core";
import 'rxjs/add/operator/toPromise';

@Component({
	selector: 'medical-record-component',
	template: require('./medicalRecord.html')
})
export class MedicalRecordComponent {
	services : PatientService[];
	selectedService : string = "";

	constructor(private medicalRecordService:MedicalRecordService) {
		this.loadServiceList();
	}

	loadServiceList() {
		this.medicalRecordService.getServices()
			.subscribe((services : PatientService[]) => {
				this.services = services;
			});
	}
}
