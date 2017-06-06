import {Organisation} from "./Organisation";
import {Group} from "./Group";
export class UserRole {

	constructor() {
	}

	uuid:string;
	name:string;
	description:string;
	group: Group;
	organisation: Organisation;
	clientRoles: UserRole[];
}
