/// <reference path="../../typings/tsd.d.ts" />

module app.models {
	export enum ItemType {
		ReportFolder,	// 0
		Report,			// 1
		Query,			// 2
		Test,				// 3
		Resource,	// 4
		CodeSet,		// 5
		DataSet,	// 6
		LibraryFolder, // 7
		Protocol, // 8
		System // 9
	}

	export function itemTypeIdToString() {
		return function (input:number):string {
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

	export function itemTypeIdToIcon() {
		return function (input:number):string {
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

	angular
		.module('app.models')
		.filter('itemTypeIdToString', itemTypeIdToString)
		.filter('itemTypeIdToIcon', itemTypeIdToIcon);
}

