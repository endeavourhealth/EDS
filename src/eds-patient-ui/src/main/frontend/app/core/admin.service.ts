import {MenuOption} from "../models/MenuOption";

export class AdminService {
	pendingChanges : boolean;

	getMenuOptions():MenuOption[] {
		return [
			{caption: 'Patients', state: 'app.medicalRecord', icon: 'fa fa-tag'},
			{caption: 'Consent', state: 'app.consent', icon: 'fa fa-check-square-o'},
			{caption: 'Audit', state: 'app.audit', icon: 'fa fa-list-ul'},
		];
	}

	setPendingChanges() : void {
		this.pendingChanges = true;
	}

	clearPendingChanges() : void {
		this.pendingChanges = false;
	}

	getPendingChanges() : boolean {
		return this.pendingChanges;
	}
}