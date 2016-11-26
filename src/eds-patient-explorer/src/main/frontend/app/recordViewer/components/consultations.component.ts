import {Component, Input} from "@angular/core";

@Component({
	selector : 'consultations',
	template : require('./consultations.html')
})
export class ConsultationsComponent {
	@Input() patientId : string;

	getConsultations() : any {
		return null;
	}
}