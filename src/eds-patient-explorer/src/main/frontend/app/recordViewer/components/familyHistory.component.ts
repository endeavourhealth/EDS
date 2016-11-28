import {Component, Input} from "@angular/core";
import {UIFamilyHistory} from "../models/resources/clinical/UIFamilyHistory";

@Component({
	selector : 'familyHistory',
	template : require('./familyHistory.html')
})
export class FamilyHistoryComponent {
	@Input() familyHistory : UIFamilyHistory[];

}