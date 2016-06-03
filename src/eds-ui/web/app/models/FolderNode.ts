module app.models {
	'use strict';

	export class FolderNode extends Folder {
		isExpanded:boolean;
		loading:boolean;
		nodes:FolderNode[];
	}
}