import {Endpoint} from "./Endpoint";

export class Service {
	uuid: string;
	localIdentifier: string;
	hasInboundError: boolean;
	name: string;
	endpoints: Endpoint[];
	organisations:{ [key:string]:string; };
	additionalInfo: string;

	constructor() {}
}
