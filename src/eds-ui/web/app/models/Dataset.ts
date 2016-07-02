module app.models {
	'use strict';

	export class DataSet {
		uuid:string;
		name:string;
		composition : Composition[];
	}
}