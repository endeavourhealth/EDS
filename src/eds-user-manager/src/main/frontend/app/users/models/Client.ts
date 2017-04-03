import {UserRole} from "./UserRole";
export class Client {

			constructor() {
	}

	uuid:string;
	name:string;
	description:string;
	clientRoles: UserRole[];
}
