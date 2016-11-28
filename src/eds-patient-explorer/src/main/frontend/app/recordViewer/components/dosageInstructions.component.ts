import {Component, Input} from "@angular/core";
import {UIDiary} from "../models/resources/clinical/UIDiary";
import {UIDosageInstruction} from "../models/resources/clinical/UIDosageInstruction";
@Component({
	selector : 'dosageInstructions',
	template : require('./dosageInstructions.html')
})
export class DosageInstructionsComponent {
	@Input() dosageInstructions : UIDosageInstruction[];

	hasAdditionalInstructions(dosageInstruction : UIDosageInstruction) {
		return dosageInstruction
			&& dosageInstruction.additionalInstructions
			&& dosageInstruction.additionalInstructions.codes
			&& dosageInstruction.additionalInstructions.codes.length > 0;
	}
}