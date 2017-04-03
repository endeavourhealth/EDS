import {Component, Input} from "@angular/core";

@Component({
	selector : 'loadingIndicator',
	template : require('./loadingIndicator.html')
})
export class LoadingIndicatorComponent {
	@Input() done : any;
}