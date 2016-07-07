module app.models {
	'use strict';

	export class Resource {
		heading : string;
		resourceUuid : string[];
		calculation : CalculationType;
		filter : FieldTest[];
		restriction : Restriction;
	}
}

