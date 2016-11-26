import {Component, Input} from "@angular/core";
import {UIMedicationOrder} from "../models/resources/clinical/UIMedicationOrder";

@Component({
	selector : 'medication',
	template : require('./medication.html')
})
export class MedicationComponent {
	@Input() medicationOrders : UIMedicationOrder[];
}