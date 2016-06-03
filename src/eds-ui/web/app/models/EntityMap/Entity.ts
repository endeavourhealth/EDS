module app.models {
	'use strict';

	export class Entity {
		logicalName : string;
		displayName : string;
		resultSetIndex : number;
		cardinality : Cardinality;
		populationFieldIndex : number;
		field : Field[];
	}
}