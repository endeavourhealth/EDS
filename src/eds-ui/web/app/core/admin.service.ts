/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../models/MenuOption.ts" />
/// <reference path="../models/Role.ts" />
/// <reference path="../models/User.ts" />
/// <reference path="../models/UserInRole.ts" />

module app.core {
	import IPromise = angular.IPromise;
	import LoginResponse = app.models.LoginResponse;
	import UserList = app.models.UserList;
	import User = app.models.User;
	'use strict';

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
				{caption: 'Library', state: 'app.library', icon: 'fa fa-book'},
				{caption: 'Reports', state: 'app.reportList', icon: 'fa fa-files-o'},
				{caption: 'Organisations', state: 'app.organisationSet', icon: 'fa fa-hospital-o'},
				{caption: 'Administration', state: 'app.admin', icon: 'fa fa-users'},
				{caption: 'Audit', state: 'app.audit', icon: 'fa fa-archive'}
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

	angular
		.module('app.core')
		.service('AdminService', AdminService);
}