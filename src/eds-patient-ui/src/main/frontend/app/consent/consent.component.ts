import {ConsentService} from "./consent.service";

import {Component, Inject} from '@angular/core'

@Component({
	selector: 'consent-component',
	template: require('./consent.html')
})
export class ConsentComponent {

	constructor(@Inject('ConsentService') consentService:ConsentService) {
	}
}

