import {Component, Input} from "@angular/core";
import {UIProblem} from "../models/resources/clinical/UIProblem";
import {UIAllergy} from "../models/resources/clinical/UIAllergy";
import {UIImmunisation} from "../models/resources/clinical/UIImmunisation";
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
	@Input() immunisations : UIImmunisation[];
}