module app.models {
	'use strict';

	export class DataSource {
		entity : string;
		dataSourceUuid : string[];
		calculation : CalculationType;
		filter : FieldTest[];
		restriction : Restriction;
	}
}