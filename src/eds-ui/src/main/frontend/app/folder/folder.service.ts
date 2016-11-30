import {Injectable} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "../core/baseHttp2.service";

import {FolderNode} from "./models/FolderNode";
import {Folder} from "./models/Folder";

@Injectable()
export class FolderService extends BaseHttp2Service {
	constructor(http : Http) { super(http); }

	getFolders(moduleId : number, folderUuid : string):Observable<{folders : FolderNode[]}> {
		let params = new URLSearchParams();
		params.append('folderType', moduleId.toString());
		params.append('parentUuid', folderUuid);

		return this.httpGet('api/folder/getFolders', { search : params });
	}

	saveFolder(folder: Folder):Observable<any> {
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

	deleteFolder(folder: Folder):Observable<string> {
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
