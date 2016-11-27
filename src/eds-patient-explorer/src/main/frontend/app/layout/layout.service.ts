import {Injectable} from "@angular/core";
import {MenuOption} from "./models/MenuOption";

@Injectable()
export class LayoutService {
	getMenuOptions():MenuOption[] {
		return [
			{caption: 'Record Viewer', state: 'app.recordViewer', icon: 'fa fa-heart'},
			{caption: 'Patients', state: 'app.patientIdentity', icon: 'fa fa-user'},
			{caption: 'Resources', state: 'app.resourceList', icon: 'fa fa-fire'},
		];
	}
}