import {Endpoint} from "./Endpoint";
import {SystemStatus} from "./SystemStatus";

export class Service {
	uuid: string;
	localIdentifier: string;
	publisherConfigName: string;
	name: string;
	endpoints: Endpoint[];
	organisations:{ [key:string]:string; };
	additionalInfo: string;
	notes: string;
	postcode: string;
	ccgCode: string;
	organisationTypeDesc: string;
	organisationTypeCode: string;
	systemStatuses: SystemStatus[];

	constructor() {}
}
