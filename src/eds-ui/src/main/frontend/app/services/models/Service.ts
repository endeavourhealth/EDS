import {Endpoint} from "./Endpoint";
import {SystemStatus} from "./SystemStatus";

export class Service {
	uuid: string;
	localIdentifier: string;
	publisherConfigName: string;
	name: string;
	endpoints: Endpoint[];
	postcode: string;
	ccgCode: string;
	organisationTypeDesc: string;
	organisationTypeCode: string;
	systemStatuses: SystemStatus[];
	alias: string;
	tags: {};

	constructor() {}


}
