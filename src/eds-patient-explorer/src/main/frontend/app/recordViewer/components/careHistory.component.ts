import {Component, Input} from "@angular/core";

@Component({
	selector : 'careHistory',
	template : require('./careHistory.html')
})
export class CareHistoryComponent {
	@Input() patientId : string;

	getCareHistory() : any {
		return null;
	}
}