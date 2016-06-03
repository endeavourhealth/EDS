/// <reference path="../../typings/tsd.d.ts" />

module app.core {
	import ItemSummaryList = app.models.ItemSummaryList;
	import FolderNode = app.models.FolderNode;
	import Folder = app.models.Folder;
	'use strict';

	export interface IFolderService {
		getFolders(moduleId : number, folderUuid : string):ng.IPromise<{folders : FolderNode[]}>;
		getFolderContents(folderId : string):ng.IPromise<ItemSummaryList>;
		saveFolder(folder : Folder):ng.IPromise<string>;
		deleteFolder(folder : Folder):ng.IPromise<any>;
	}

	export class FolderService extends BaseHttpService implements IFolderService {
		getFolders(moduleId : number, folderUuid : string):ng.IPromise<{folders : FolderNode[]}> {
			var request = {
				params: {
					'folderType': moduleId,
					'parentUuid': folderUuid
				}
			};

			return this.httpGet('api/folder/getFolders', request);
		}

		getFolderContents(folderUuid : string):ng.IPromise<ItemSummaryList> {
			var request = {
				params: {
					'folderUuid': folderUuid
				}
			};
			return this.httpGet('api/folder/getFolderContents', request);
		}

		saveFolder(folder: Folder):ng.IPromise<string> {
			// Make clean copy of object, just in case of additions
			// Typing the request ensures any property changes are caught
			var request : Folder = {
				uuid : folder.uuid,
				folderName : folder.folderName,
				folderType : folder.folderType,
				parentFolderUuid : folder.parentFolderUuid,
				hasChildren : folder.hasChildren,
				contentCount : folder.contentCount
			};

			return this.httpPost('api/folder/saveFolder', request);
		}

		deleteFolder(folder: Folder):ng.IPromise<string> {
			// Make clean copy of object, just in case of additions
			// Typing the request ensures any property changes are caught
			var request : Folder = {
				uuid : folder.uuid,
				folderName : folder.folderName,
				folderType : folder.folderType,
				parentFolderUuid : folder.parentFolderUuid,
				hasChildren : folder.hasChildren,
				contentCount : folder.contentCount
			};

			return this.httpPost('api/folder/deleteFolder', request);
		}
	}

	angular
		.module('app.core')
		.service('FolderService', FolderService);
}