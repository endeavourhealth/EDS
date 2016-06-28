module app.models {
	'use strict';

	export class System {
		uuid:string;
		name:string;
		technicalInterface:TechnicalInterface[];
	}
}