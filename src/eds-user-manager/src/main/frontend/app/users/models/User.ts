import {UserRole} from "./UserRole";
export class User {

			constructor() {
	}

	uuid:string;
	title:string;
	forename:string;
	surname:string;
	username:string;
	password:string;
	email:string;
	mobile:string;
	photo:string;
	totp: string;
	defaultOrgId: string;
	userRoles: UserRole[];

	isSuperUser:boolean;
	permissions:string[];

	displayName():string {
		if(this.forename == null && this.surname == null) {
			if(this.uuid != null) {
				return this.uuid;
			}
			return 'Unknown User';
		}

		var displayName = this.forename + ' ' + this.surname;

		if(this.title != null) {
			displayName = this.title + ' ' + displayName;
		}

		return displayName.trim();
	}
}
