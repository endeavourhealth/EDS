import {Endpoint} from "./Endpoint";

export class Service {
	uuid: string;
	localIdentifier: string;
	publisherConfigName: string;
	hasInboundError: boolean;
	name: string;
	endpoints: Endpoint[];
	organisations:{ [key:string]:string; };
	additionalInfo: string;
	notes: string;
	postcode: string;
	ccgCode: string;
	organisationTypeDesc: string;
	organisationTypeCode: string;

	constructor() {}
}
