import {Injectable} from "@angular/core";
import {Http} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {MenuOption} from "../models/MenuOption";
import {UserList} from "../models/UserList";
import {User} from "../models/User";

@Injectable()
export class AdminService extends BaseHttp2Service{
	constructor(http : Http) { super (http); }

	pendingChanges : boolean;

	getMenuOptions():MenuOption[] {
		return [
			{caption: 'Dashboard', state: 'dashboard', icon: 'fa fa-tachometer'},
			{caption: 'Protocols', state: 'library', icon: 'fa fa-share-alt'},
			{caption: 'Organisations', state: 'organisationList', icon: 'fa fa-hospital-o'},
			{caption: 'Services', state: 'serviceList', icon: 'fa fa-building-o'},
			{caption: 'Queueing', state: 'queueing', icon: 'fa fa-tasks'},
			//
			{caption: 'Patients', state: 'patientIdentity', icon: 'fa fa-user'},
			{caption: 'Record Viewer', state: 'recordViewer', icon: 'fa fa-heart'},
			{caption: 'Resources', state: 'resourceList', icon: 'fa fa-fire'},
			//
			{caption: 'Monitoring', state: 'monitoring', icon: 'fa fa-list-alt'},
			{caption: 'Transform Errors', state: 'transformErrors', icon: 'fa fa-exchange'},
			{caption: 'Statistics', state: 'stats', icon: 'fa fa-line-chart'},
			{caption: 'Audit', state: 'audit', icon: 'fa fa-list-ul'}
			// {caption: 'Admin', state: 'admin', icon: 'fa fa-user'}
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


	getUserList() : Observable<UserList> {
		return this.httpGet('/api/admin/getUsers');
	}

	saveUser(user : User) : Observable<{uuid : string}> {
		return this.httpPost('/api/admin/saveUser', user);
	}
}