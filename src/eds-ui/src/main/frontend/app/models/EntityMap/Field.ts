import {DataValueType} from "./DataValueType";
import {LogicalDataType} from "./LogicalDataType";

export class Field {
	logicalName : string;
	displayName : string;
	index : number;
	availability : string[];
	logicalDataType : LogicalDataType;
	dataValues : DataValueType[];
}
