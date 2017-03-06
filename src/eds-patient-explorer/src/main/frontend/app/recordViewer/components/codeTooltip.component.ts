import {Component, Input} from "@angular/core";
import {UICode} from "../models/types/UICode";

@Component({
	selector : 'codeTooltip',
	template : require('./codeTooltip.html')
})
export class CodeTooltipComponent {
	@Input() code : UICode;

	private getSystem(system : string) : string {
		return system.substr(system.lastIndexOf('/') + 1);
	}
}