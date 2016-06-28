module app.models {
	'use strict';

	export class LibraryItem {
		uuid:string;
		name:string;
		description:string;
		folderUuid:string;
		query:Query;
		// dataSource:DataSource;
		// test:Test;
		codeSet:CodeSet;
		listReport:ListReport;
		protocol:Protocol;
		system:System;
	}
}