module app.models {
	'use strict';

	export class CodeSelection extends Code {
		includeChildren: boolean;
		exclusions: Code[];
	}
}