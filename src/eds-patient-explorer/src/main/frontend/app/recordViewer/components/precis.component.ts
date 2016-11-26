import {Component, Input} from "@angular/core";
import {UIPatientRecord} from "../models/UIPatientRecord";

@Component({
	selector : 'precis',
	template : require('./precis.html')
})
export class PrecisComponent {
	@Input() patient : UIPatientRecord;
}