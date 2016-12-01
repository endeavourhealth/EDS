import {Component, Input} from "@angular/core";
import {UIObservation} from "../models/resources/clinical/UIObservation";

@Component({
	selector : 'investigations',
	template : require('./investigations.html')
})
export class InvestigationsComponent {
	@Input() investigations : UIObservation[];
}