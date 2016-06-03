

 module app.models {
	'use strict';

	export class Report {
		uuid:string;
		name:string;
		description:string;
		folderUuid:string;
		reportItem:ReportItem[];
	}
}