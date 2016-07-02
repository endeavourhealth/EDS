module app.models {
	'use strict';

	export class LibraryItem {
		uuid:string;
		name:string;
		description:string;
		folderUuid:string;
		query:Query;
		// resource:Resource;
		// test:Test;
		codeSet:CodeSet;
		dataSet:DataSet;
		protocol:Protocol;
		system:System;
	}
}