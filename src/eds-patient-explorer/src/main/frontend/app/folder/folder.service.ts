import {Injectable} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {FolderNode} from "./models/FolderNode";

@Injectable()
export class FolderService extends BaseHttp2Service {
	constructor(http : Http) { super(http); }

	getFolders(moduleId : number, folderUuid : string):Observable<{folders : FolderNode[]}> {
		let params = new URLSearchParams();
		params.append('folderType', moduleId.toString());
		params.append('parentUuid', folderUuid);

		return this.httpGet('api/folder/getFolders', { search : params });
	}
}
