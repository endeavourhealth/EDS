import {Endpoint} from "./Endpoint";

export class Service {
	uuid:string;
	localIdentifier : string;
	name:string;
	endpoints:Endpoint[];
	organisations:{ [key:string]:string; };

	constructor() {}
}
