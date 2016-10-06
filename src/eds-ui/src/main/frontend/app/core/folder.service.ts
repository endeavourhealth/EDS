import {FolderNode} from "../models/FolderNode";
import {ItemSummaryList} from "../models/ItemSummaryList";
import {Folder} from "../models/Folder";
import {BaseHttpService} from "./baseHttp.service";

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
