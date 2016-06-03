/// <reference path="../../typings/tsd.d.ts" />

module app.core {
	import LibraryItem = app.models.LibraryItem;
	import EntityMap = app.models.EntityMap;
	'use strict';

	export interface ILibraryService {
		getLibraryItem(uuid : string):ng.IPromise<LibraryItem>;
		saveLibraryItem(libraryItem : LibraryItem):ng.IPromise<LibraryItem>;
		deleteLibraryItem(uuid : string):ng.IPromise<any>;
		getEntityMap():ng.IPromise<EntityMap>;
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
	}

	angular
		.module('app.core')
		.service('LibraryService', LibraryService);
}