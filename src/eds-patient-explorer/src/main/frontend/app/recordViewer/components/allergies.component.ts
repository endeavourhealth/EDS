import {Component, Input} from "@angular/core";
import {UIAllergy} from "../models/resources/clinical/UIAllergy";

@Component({
	selector : 'allergies',
	template : require('./allergies.html')
})
export class AllergiesComponent {
	@Input() allergies : UIAllergy[];

}