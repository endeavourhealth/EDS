/// <reference path="../../typings/index.d.ts" />
/// <reference path="../models/MenuOption.ts" />

module app.core {
	import IPromise = angular.IPromise;
	'use strict';

	export interface IAdminService {
		getMenuOptions() : app.models.MenuOption[];

		setPendingChanges() : void;
		clearPendingChanges() : void;
		getPendingChanges() : boolean;
	}

	export class AdminService extends BaseHttpService implements IAdminService {
		pendingChanges : boolean;

		getMenuOptions():app.models.MenuOption[] {
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

	angular
		.module('app.core')
		.service('AdminService', AdminService);
}