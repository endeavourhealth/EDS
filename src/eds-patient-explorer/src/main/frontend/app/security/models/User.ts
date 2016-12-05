import {OrganisationGroup} from "./OrganisationGroup";
export class User {

			constructor() {
	}

	uuid:string;
	title:string;
	forename:string;
	surname:string;
	username:string;	// email
	isSuperUser:boolean;
	permissions:string[];
	organisation:string;
	organisationGroups: OrganisationGroup[];

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
