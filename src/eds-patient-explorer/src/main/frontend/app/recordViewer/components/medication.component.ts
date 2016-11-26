import {Component, Input} from "@angular/core";

@Component({
	selector : 'medication',
	template : require('./medication.html')
})
export class MedicationComponent {
	@Input() patientId : string;

	getMedication() : any {
		return null;
	}
}