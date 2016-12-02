import {Cardinality} from "./Cardinality";
import {Field} from "./Field";

export class Entity {
	logicalName : string;
	displayName : string;
	resultSetIndex : number;
	cardinality : Cardinality;
	populationFieldIndex : number;
	field : Field[];
}
