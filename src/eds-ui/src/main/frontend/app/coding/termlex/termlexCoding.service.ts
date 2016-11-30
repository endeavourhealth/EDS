import {Injectable} from "@angular/core";

import {BaseHttp2Service} from "../../core/baseHttp2.service";
import {ICodingService} from "../coding.service";
import {CodeSetValue} from "../models/CodeSetValue";
import {TermlexSearchResult} from "./TermlexSearchResult";
import {TermlexCode} from "./TermlexCode";
import {Concept} from "../models/Concept";
import {Observable} from "rxjs";
import {Http, URLSearchParams} from "@angular/http";

@Injectable()
export class TermlexCodingService extends BaseHttp2Service implements ICodingService {
	constructor(http : Http) { super(http); }

	searchCodes(searchData : string) : Observable<CodeSetValue[]> {
		var vm = this;
		var params = new URLSearchParams();
		params.append('term', searchData);
		params.append('maxResultsSize', '20');
		params.append('start', '0');

		var observable = Observable.create(observer => {
			vm.httpGet('http://termlex.org/search/sct', { search : params, withCredentials : false })
				.subscribe(
					(response) => {
					var termlexResult : TermlexSearchResult = response as TermlexSearchResult;
					var matches : CodeSetValue[] = termlexResult.results.map((t) => vm.termlexCodeToCodeSetValue(t));
					observer.next(matches);
				},
				(exception) =>
					observer.error(exception)
			);
		});
		return observable;
	}

	getCodeChildren(id : string) : Observable<CodeSetValue[]> {
		var vm = this;

		var observable = Observable.create(observer => {
			vm.httpGet('http://termlex.org/hierarchy/' + id + '/childHierarchy', { withCredentials : false })
				.subscribe(
					(response) => {
					var termlexResult : TermlexCode[] = response.data as TermlexCode[];
					var matches : CodeSetValue[] = termlexResult.map((t) => vm.termlexCodeToCodeSetValue(t));
					observer.next(matches);
				},
				(exception) => observer.error(exception)
				);
		});


		return observable;
	}

	getCodeParents(id : string):Observable<CodeSetValue[]> {
		var vm = this;
		var observable = Observable.create(observer => {
			vm.httpGet('http://termlex.org/hierarchy/' + id + '/parentHierarchy', { withCredentials : false })
				.subscribe(
				(response) => {
					var termlexResult : TermlexCode[] = response.data as TermlexCode[];
					var matches : CodeSetValue[] = termlexResult.map((t) => vm.termlexCodeToCodeSetValue(t));
					observer.next(matches);
				},
				(exception) => observer.error(exception)
				);
		});

		return observable;
	}

	termlexCodeToCodeSetValue(termlexCode : TermlexCode) : CodeSetValue {
		var codeSetValue : CodeSetValue = {
			code : termlexCode.id,
			includeChildren : null,
			exclusion : null
		};
		return codeSetValue;
	}

	getPreferredTerm(id : string):Observable<Concept> {
		return this.httpGet('http://termlex.org/concepts/' + id + '/?flavour=ID_LABEL', { withCredentials : false });
	}
}
