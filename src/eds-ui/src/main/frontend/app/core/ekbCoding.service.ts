import {BaseHttpService} from "./baseHttp.service";
import {ICodingService} from "./coding.service";
import {CodeSetValue} from "../models/CodeSetValue";
import {Concept} from "../models/Concept";

export class EkbCodingService extends BaseHttpService implements ICodingService {

	searchCodes(searchData : string):ng.IPromise<CodeSetValue[]> {
		var vm = this;
		var request = {
			params: {
				'term': searchData,
				'maxResultsSize': 20,
				'start': 0
			}
		};
		var defer = vm.promise.defer();
		vm.http.get('/api/ekb/search/sct', request)
			.then(function (response) {
				defer.resolve(response.data);
			})
			.catch(function (exception) {
				defer.reject(exception);
			});

		return defer.promise;
	}

	getCodeChildren(id : string):ng.IPromise<CodeSetValue[]> {
		var vm = this;
		var defer = vm.promise.defer();
		vm.http.get('/api/ekb/hierarchy/' + id + '/childHierarchy')
			.then(function (response) {
				defer.resolve(response.data);
			})
			.catch(function (exception) {
				defer.reject(exception);
			});

		return defer.promise;
	}

	getCodeParents(id : string):ng.IPromise<CodeSetValue[]> {
		var vm = this;
		var defer = vm.promise.defer();
		vm.http.get('/api/ekb/hierarchy/' + id + '/parentHierarchy')
			.then(function (response) {
				defer.resolve(response.data);
			})
			.catch(function (exception) {
				defer.reject(exception);
			});

		return defer.promise;
	}

	getPreferredTerm(id : string):ng.IPromise<Concept> {
		return this.httpGet('/api/ekb/concepts/' + id);
	}
}
