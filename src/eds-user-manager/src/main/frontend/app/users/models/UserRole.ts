import {Organisation} from "./Organisation";
export class UserRole {

	constructor() {
	}

	uuid:string;
	name:string;
	description:string;
	organisation: Organisation;
	clientRoles: UserRole[];
}
