	import {LibraryItem} from "../models/LibraryItem";
	import {System} from "../models/System";
	import {BaseHttpService} from "./baseHttp.service";
	import {EntityMap} from "../models/EntityMap/EntityMap";
	import {Cohort} from "../models/Cohort";
	import {DataSet} from "../models/Dataset";

	export interface ILibraryService {
		getLibraryItem(uuid : string):ng.IPromise<LibraryItem>;
		saveLibraryItem(libraryItem : LibraryItem):ng.IPromise<LibraryItem>;
		deleteLibraryItem(uuid : string):ng.IPromise<any>;
		getEntityMap():ng.IPromise<EntityMap>;
		getSystems():ng.IPromise<System[]>;
		getCohorts():ng.IPromise<Cohort[]>;
		getDatasets():ng.IPromise<DataSet[]>;
		getProtocols(serviceId : string):ng.IPromise<LibraryItem[]>;
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

		getDatasets():ng.IPromise<DataSet[]> {
			return this.httpGet('api/library/getDataSets');
		}

		getProtocols(serviceId : string):ng.IPromise<LibraryItem[]> {
			var request = {
				params: {
					'serviceId': serviceId
				}
			};

			return this.httpGet('api/library/getProtocols', request);
		}
	}
