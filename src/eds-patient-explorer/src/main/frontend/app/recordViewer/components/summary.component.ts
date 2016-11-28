import {Component, Input} from "@angular/core";
import {UIProblem} from "../models/resources/clinical/UIProblem";
import {UIMedicationOrder} from "../models/resources/clinical/UIMedicationOrder";
import {UIAllergy} from "../models/resources/clinical/UIAllergy";
import {UIImmunization} from "../models/resources/clinical/UIImmunization";

@Component({
	selector : 'summary',
	template : require('./summary.html')
})
export class SummaryComponent {
	@Input() activeProblems : UIProblem[];
	@Input() pastProblems : UIProblem[];
	@Input() allergies : UIAllergy[];
	@Input() currentMedication : UIMedicationOrder[];
	@Input() pastMedication : UIMedicationOrder[];
	@Input() immunizations : UIImmunization[];
}