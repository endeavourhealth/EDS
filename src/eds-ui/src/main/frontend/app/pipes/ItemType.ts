import { Pipe, PipeTransform } from '@angular/core';
import {ItemType} from "../models/ItemType";

@Pipe({name: 'itemTypeIdToString'})
export class ItemTypeIdToStringPipe implements PipeTransform {
	transform (input:number):string {
		switch (input) {
			case ItemType.ReportFolder:
				return 'Report folder';
			case ItemType.Report:
				return 'Report';
			case ItemType.Query:
				return 'Cohort';
			case ItemType.Test:
				return 'Test';
			case ItemType.Resource:
				return 'Resource';
			case ItemType.CodeSet:
				return 'Code set';
			case ItemType.DataSet:
				return 'Data set';
			case ItemType.LibraryFolder:
				return 'Library folder';
			case ItemType.Protocol:
				return 'Data protocol';
			case ItemType.System:
				return 'System';
		}
	};
}

@Pipe({name: 'itemTypeIdToIcon'})
export class ItemTypeIdToIconPipe implements PipeTransform {
	transform (input:number):string {
		switch (input) {
			case ItemType.ReportFolder:
				return 'fa-folder-open';
			case ItemType.Report:
				return 'fa-file';
			case ItemType.Query:
				return 'fa-user';
			case ItemType.Test:
				return 'fa-random';
			case ItemType.Resource:
				return 'fa-database';
			case ItemType.CodeSet:
				return 'fa-tags';
			case ItemType.DataSet:
				return 'fa-list-alt';
			case ItemType.LibraryFolder:
				return 'fa-folder-open';
			case ItemType.Protocol:
				return 'fa-share-alt';
			case ItemType.System:
				return 'fa-laptop';
		}
	};
}
