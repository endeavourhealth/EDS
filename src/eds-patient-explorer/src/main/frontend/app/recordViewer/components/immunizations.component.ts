import {Component, Input} from "@angular/core";
import {UIImmunization} from "../models/resources/clinical/UIImmunization";

@Component({
	selector : 'immunizations',
	template : require('./immunizations.html')
})
export class ImmunizationsComponent {
	@Input() immunizations : UIImmunization[];

}