import {Component, Input} from "@angular/core";
import {UIProblem} from "../models/resources/clinical/UIProblem";
import {UIAllergy} from "../models/resources/clinical/UIAllergy";
import {UIImmunization} from "../models/resources/clinical/UIImmunization";
import {UIMedicationStatement} from "../models/resources/clinical/UIMedicationStatement";

@Component({
	selector : 'summary',
	template : require('./summary.html')
})
export class SummaryComponent {
	@Input() activeProblems : UIProblem[];
	@Input() pastProblems : UIProblem[];
	@Input() allergies : UIAllergy[];
	@Input() acuteMedication : UIMedicationStatement[];
	@Input() repeatMedication : UIMedicationStatement[];
	@Input() immunizations : UIImmunization[];
}