module app.models {
	'use strict';

	export class Field {
		logicalName : string;
		displayName : string;
		index : number;
		availability : string[];
		logicalDataType : LogicalDataType;
		dataValues : DataValueType[];
	}
}