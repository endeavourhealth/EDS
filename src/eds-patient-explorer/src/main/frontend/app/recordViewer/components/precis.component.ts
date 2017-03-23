import {Component, Input} from "@angular/core";
import {UIPatient} from "../models/resources/admin/UIPatient";

@Component({
	selector : 'precis',
	template : require('./precis.html')
})
export class PrecisComponent {
	@Input() person : UIPatient;
	@Input() episode : any;
}