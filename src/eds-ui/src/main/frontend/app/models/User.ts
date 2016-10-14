import {UserInRole} from "./UserInRole";
import S = require("string");

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
	userInRoles:UserInRole[];
	currentUserInRoleUuid:string;

	displayName():string {
		if(this.forename == null && this.surname == null)
			if(this.uuid != null)
				return this.uuid;

        return this.formatName(this.title, this.forename, this.surname);
	}

	private formatName(title: string, forename: string, surname: string) {
        if (S(surname).isEmpty())
            surname = "UNKNOWN";

        let result: string = S(surname).trim().toString().toUpperCase();

        if (!S(forename).isEmpty())
            result += ", " + S(forename).trim().capitalize();

        if (!S(title).isEmpty())
            result += " (" + S(title).trim().capitalize() + ")";

        return result;
    }
}
