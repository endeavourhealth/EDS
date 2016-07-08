module app.models {
	'use strict';

	export class Service {
		uuid:string;
		localIdentifier : string;
		name:string;
		organisations:{ [key:string]:string; };

		constructor() {}
	}
}