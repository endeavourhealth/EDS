import {BaseHttpService} from "./baseHttp.service";
import {ICodingService} from "./coding.service";
import {CodeSetValue} from "../models/CodeSetValue";
import {TermlexSearchResult} from "../models/TermlexSearchResult";
import {TermlexCode} from "../models/TermlexCode";
import {Concept} from "../models/Concept";

export class TermlexCodingService extends BaseHttpService implements ICodingService {

	searchCodes(searchData : string):ng.IPromise<CodeSetValue[]> {
		var vm = this;
		var request = {
			params: {
				'term': searchData,
				'maxResultsSize': 20,
				'start': 0
			},
			withCredentials: false
		};
		var defer = vm.promise.defer();
		vm.http.get('http://termlex.org/search/sct', request)
			.then(function (response) {
				var termlexResult : TermlexSearchResult = response.data as TermlexSearchResult;
				var matches : CodeSetValue[] = termlexResult.results.map((t) => vm.termlexCodeToCodeSetValue(t));
				defer.resolve(matches);
			})
			.catch(function (exception) {
				defer.reject(exception);
			});

		return defer.promise;
	}

	getCodeChildren(id : string):ng.IPromise<CodeSetValue[]> {
		var vm = this;
		var request = { withCredentials: false };
		var defer = vm.promise.defer();
		vm.http.get('http://termlex.org/hierarchy/' + id + '/childHierarchy', request)
			.then(function (response) {
				var termlexResult : TermlexCode[] = response.data as TermlexCode[];
				var matches : CodeSetValue[] = termlexResult.map((t) => vm.termlexCodeToCodeSetValue(t));
				defer.resolve(matches);
			})
			.catch(function (exception) {
				defer.reject(exception);
			});

		return defer.promise;
	}

	getCodeParents(id : string):ng.IPromise<CodeSetValue[]> {
		var vm = this;
		var request = { withCredentials: false };
		var defer = vm.promise.defer();
		vm.http.get('http://termlex.org/hierarchy/' + id + '/parentHierarchy', request)
			.then(function (response) {
				var termlexResult : TermlexCode[] = response.data as TermlexCode[];
				var matches : CodeSetValue[] = termlexResult.map((t) => vm.termlexCodeToCodeSetValue(t));
				defer.resolve(matches);
			})
			.catch(function (exception) {
				defer.reject(exception);
			});

		return defer.promise;
	}

	termlexCodeToCodeSetValue(termlexCode : TermlexCode) : CodeSetValue {
		var codeSetValue : CodeSetValue = {
			code : termlexCode.id,
			includeChildren : null,
			exclusion : null
		};
		return codeSetValue;
	}

	getPreferredTerm(id : string):ng.IPromise<Concept> {
		var request = { withCredentials: false };
		return this.httpGet('http://termlex.org/concepts/' + id + '/?flavour=ID_LABEL', request);
	}
}
