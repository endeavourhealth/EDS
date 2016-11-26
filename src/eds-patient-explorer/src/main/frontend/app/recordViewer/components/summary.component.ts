import {Component, Input} from "@angular/core";

@Component({
	selector : 'summary',
	template : require('./summary.html')
})
export class SummaryComponent {
	@Input() patientId : string;

	getSummary() : any {
		return null;
	}
}