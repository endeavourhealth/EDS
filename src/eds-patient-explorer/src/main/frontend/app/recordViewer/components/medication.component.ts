import {Component, Input} from "@angular/core";
import {UIMedicationStatement} from "../models/resources/clinical/UIMedicationStatement";

@Component({
	selector : 'medication',
	template : require('./medication.html')
})
export class MedicationComponent {
	@Input() title : string;
	@Input() medication : UIMedicationStatement[];
	@Input() showDateEnded : boolean = false;
	@Input() placeholder : string;
}