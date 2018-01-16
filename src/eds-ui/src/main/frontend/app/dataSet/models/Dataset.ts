import {Composition} from "./Composition";

export class DataSet {
	uuid:string;
	name:string;
	description:string;
	attributes:string;
	queryDefinition:string;
	dpas : { [key:string]:string; };

	composition : Composition[];

	getDisplayItems() :any[] {
		return [
			{label: 'Description', property: 'description'}
		];
	}
}
