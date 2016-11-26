import {Injectable} from "@angular/core";
import {MenuOption} from "./models/MenuOption";

@Injectable()
export class LayoutService {
	getMenuOptions():MenuOption[] {
		return [
			{caption: 'Record Viewer', state: 'recordViewer', icon: 'fa fa-heart'},
			{caption: 'Patients', state: 'patientIdentity', icon: 'fa fa-user'},
			{caption: 'Resources', state: 'resourceList', icon: 'fa fa-fire'},
		];
	}
}