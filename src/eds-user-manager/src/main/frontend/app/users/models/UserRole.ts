import {Organisation} from "./Organisation";
export class UserRole {

	constructor() {
	}

	uuid:string;
	name:string;
	description:string;
	//isClient:boolean;
	organisation: Organisation;
	clientRoles: UserRole[];
}
