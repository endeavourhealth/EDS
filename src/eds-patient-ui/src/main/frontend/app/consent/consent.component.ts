import {ConsentService} from "./consent.service";

import {Component} from '@angular/core'

@Component({
	selector: 'consent-component',
	template: require('./consent.html')
})
export class ConsentComponent {

	constructor(private consentService:ConsentService) {
	}
}

