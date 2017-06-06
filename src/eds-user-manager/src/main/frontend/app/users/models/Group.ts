export class Group {

	constructor() {
	}

	uuid:string;
	name:string;
	organisationId: string;
	isHidden: boolean = null;
	subGroups: Group[];
}
