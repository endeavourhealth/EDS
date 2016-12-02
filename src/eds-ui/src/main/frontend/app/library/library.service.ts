import {Injectable} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {LibraryItem} from "./models/LibraryItem";
import {ItemSummaryList} from "./models/ItemSummaryList";

@Injectable()
export class LibraryService extends BaseHttp2Service {
	constructor(http: Http) {
		super(http);
	}

	getFolderContents(folderUuid : string):Observable<ItemSummaryList> {
		let params = new URLSearchParams();
		params.append('folderUuid', folderUuid);
		return this.httpGet('api/folder/getFolderContents', { search : params });
	}

	getLibraryItem(uuid: string): Observable<LibraryItem> {
		let params = new URLSearchParams();
		params.set('uuid', uuid);
		return this.httpGet('api/library/getLibraryItem', {search: params});
	}

	saveLibraryItem(libraryItem: LibraryItem): Observable<LibraryItem> {
		return this.httpPost('api/library/saveLibraryItem', libraryItem);
	}

	deleteLibraryItem(uuid: string): Observable<any> {
		var libraryItem = {uuid: uuid};
		return this.httpPost('api/library/deleteLibraryItem', libraryItem);
	}
}
