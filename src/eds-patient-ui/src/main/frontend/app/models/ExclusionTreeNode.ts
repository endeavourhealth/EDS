module app.models {
	'use strict';

	export class ExclusionTreeNode {
		codeSetValue : CodeSetValue;
		children : ExclusionTreeNode[];
	}
}