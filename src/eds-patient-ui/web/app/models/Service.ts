module app.models {
	'use strict';

	export class Service {
		uuid:string;
		name:string;
		organisations:{ [key:string]:string; };

		constructor() {}
	}
}