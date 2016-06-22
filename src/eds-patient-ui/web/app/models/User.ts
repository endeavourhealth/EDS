/// <reference path="UserInRole.ts" />

module app.models {
	'use strict';

	export class User {
		uuid:string;
		title:string;
		forename:string;
		surname:string;
		username:string;	// email
		isSuperUser:boolean;
		permissions:number;

		userInRoles:UserInRole[];
		currentUserInRoleUuid:string;
	}
}