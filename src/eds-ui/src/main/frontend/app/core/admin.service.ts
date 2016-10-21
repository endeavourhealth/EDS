import IPromise = angular.IPromise;

import {User} from "../models/User";
import {UserList} from "../models/UserList";
import {BaseHttpService} from "./baseHttp.service";

export interface IAdminService {
	getMenuOptions() : app.models.MenuOption[];

	setPendingChanges() : void;
	clearPendingChanges() : void;
	getPendingChanges() : boolean;

	getUserList() : IPromise<UserList>;
	saveUser(user : User) : IPromise<{uuid : string}>;
}

export class AdminService extends BaseHttpService implements IAdminService {
	pendingChanges : boolean;

	getMenuOptions():app.models.MenuOption[] {
		return [
			{caption: 'Dashboard', state: 'app.dashboard', icon: 'fa fa-tachometer'},
			{caption: 'Protocols', state: 'app.library', icon: 'fa fa-share-alt'},
			{caption: 'Organisations', state: 'app.organisation', icon: 'fa fa-hospital-o'},
			{caption: 'Services', state: 'app.service', icon: 'fa fa-building-o'},
			{caption: 'Queueing', state: 'app.routeGroup', icon: 'fa fa-tasks'},

			{caption: 'Patients', state: 'app.patientIdentity', icon: 'fa fa-user'},
			{caption: 'Record Viewer', state: 'app.recordViewer', icon: 'fa fa-heart'},
			{caption: 'Resources', state: 'app.resources', icon: 'fa fa-fire'},

			{caption: 'Monitoring', state: 'app.logging', icon: 'fa fa-list-alt'},
			{caption: 'Transform Errors', state: 'app.transformErrors', icon: 'fa fa-exchange'},
			{caption: 'Statistics', state: 'app.stats', icon: 'fa fa-line-chart'},
			{caption: 'Audit', state: 'app.audit', icon: 'fa fa-list-ul'}
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

	getUserList() : IPromise<UserList> {
		return this.httpGet('/api/admin/getUsers');
	}

	saveUser(user : User) : IPromise<{uuid : string}> {
		return this.httpPost('/api/admin/saveUser', user);
	}
}
