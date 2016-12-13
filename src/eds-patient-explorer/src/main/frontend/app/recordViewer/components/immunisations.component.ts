import {Component, Input} from "@angular/core";
import {UIImmunisation} from "../models/resources/clinical/UIImmunisation";

@Component({
	selector : 'immunisations',
	template : require('./immunisations.html')
})
export class ImmunisationsComponent {
	@Input() immunisations : UIImmunisation[];

}