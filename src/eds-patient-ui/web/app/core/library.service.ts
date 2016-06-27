/// <reference path="../../typings/tsd.d.ts" />

module app.core {
	import LibraryItem = app.models.LibraryItem;
	import EntityMap = app.models.EntityMap;
	import FolderNode = app.models.FolderNode;
	import System = app.models.System;
	import Cohort = app.models.Cohort;
	import Dataset = app.models.Dataset;
	'use strict';

	export interface ILibraryService {
		getLibraryItem(uuid : string):ng.IPromise<LibraryItem>;
		saveLibraryItem(libraryItem : LibraryItem):ng.IPromise<LibraryItem>;
		deleteLibraryItem(uuid : string):ng.IPromise<any>;
		getEntityMap():ng.IPromise<EntityMap>;
		getSystems():ng.IPromise<System[]>;
		getCohorts():ng.IPromise<Cohort[]>;
		getDatasets():ng.IPromise<Dataset[]>;
	}

	export class LibraryService extends BaseHttpService implements ILibraryService {

		getLibraryItem(uuid : string):ng.IPromise<LibraryItem> {
			var request = {
				params: {
					'uuid': uuid
				}
			};
			return this.httpGet('api/library/getLibraryItem', request);
		}

		saveLibraryItem(libraryItem : LibraryItem):ng.IPromise<LibraryItem> {
			return this.httpPost('api/library/saveLibraryItem', libraryItem);
		}

		deleteLibraryItem(uuid : string):ng.IPromise<any> {
			var request = {
				uuid : uuid
			};
			return this.httpPost('api/library/deleteLibraryItem', request);
		}

		getEntityMap():ng.IPromise<EntityMap> {
			return this.httpGet('api/entity/getEntityMap');
		}

		getSystems():ng.IPromise<System[]> {
			return this.httpGet('api/library/getSystems');
		}

		getCohorts():ng.IPromise<Cohort[]> {
			return this.httpGet('api/library/getQueries');
		}

		getDatasets():ng.IPromise<Dataset[]> {
			return this.httpGet('api/library/getListReports');
		}
	}

	angular
		.module('app.core')
		.service('LibraryService', LibraryService);
}