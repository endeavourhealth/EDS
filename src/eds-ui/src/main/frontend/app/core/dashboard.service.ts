import {FolderItem} from "../models/FolderContent";
import {BaseHttpService} from "./baseHttp.service";

export interface IDashboardService {
	getRecentDocumentsData() : ng.IPromise<FolderItem[]>;
}

export class DashboardService extends BaseHttpService implements IDashboardService {

	getRecentDocumentsData():ng.IPromise<FolderItem[]> {
		var request = {
			params: {
				'count': 5
			}
		};

		return this.httpGet('api/dashboard/getRecentDocuments', request);
	}
}
