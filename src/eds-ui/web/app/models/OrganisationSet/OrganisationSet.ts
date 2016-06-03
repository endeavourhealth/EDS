module app.models {
	'use strict';

	export class OrganisationSet {
		uuid : string;
		name : string;
		organisations : OrganisationSetMember[];
	}
}