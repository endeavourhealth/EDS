import {Component, Input} from "@angular/core";
import {UIObservation} from "../models/resources/clinical/UIObservation";

@Component({
	selector : 'careHistory',
	template : require('./careHistory.html')
})
export class CareHistoryComponent {
	@Input() observations : UIObservation[];
}