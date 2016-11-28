import {Component, Input} from "@angular/core";
import {UIFamilyHistoryCondition} from "../models/resources/clinical/UIFamilyHistoryCondition";

@Component({
	selector : 'familyHistoryCondition',
	template : require('./familyHistoryCondition.html')
})
export class FamilyHistoryConditionComponent {
	@Input() conditions : UIFamilyHistoryCondition[];

}