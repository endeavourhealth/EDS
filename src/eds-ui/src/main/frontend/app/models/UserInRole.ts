/// <reference path="Role.ts" />

module app.models {
	'use strict';

	export class UserInRole {
		userInRoleUuid:string;
		uuid:string;
		organisationName:string;
		endUserRole:Role;
	}
}