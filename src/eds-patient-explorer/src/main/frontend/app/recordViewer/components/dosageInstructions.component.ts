import {Component, Input} from "@angular/core";
import {UIDiary} from "../models/resources/clinical/UIDiary";
import {UIDosageInstruction} from "../models/resources/clinical/UIDosageInstruction";
@Component({
	selector : 'dosageInstructions',
	template : require('./dosageInstructions.html')
})
export class DosageInstructionsComponent {
	@Input() dosageInstructions : UIDosageInstruction[];
}