import {BaseHttp2Service} from "../../core/baseHttp2.service";
import {CodingService} from "../coding.service";
import {CodeSetValue} from "../models/CodeSetValue";
import {Concept} from "../models/Concept";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";

@Injectable()
export class EkbCodingService extends BaseHttp2Service implements CodingService {
	constructor (http : Http) { super(http); }

	searchCodes(searchData : string):Observable<CodeSetValue[]> {
		var vm = this;
		var params = new URLSearchParams();
		params.append('term', searchData);
		params.append('maxResultsSize', '20');
		params.append('start', '0');

		var observable = Observable.create(observer => {
			vm.httpGet('/api/ekb/search/sct', {search: params})
				.subscribe(
					(response) => observer.next(response),
					(exception) => observer.error(exception)
				);
		});

		return observable;
	}

	getCodeChildren(id : string):Observable<CodeSetValue[]> {
		var vm = this;

		var observable = Observable.create(observer => {
			vm.httpGet('/api/ekb/hierarchy/' + id + '/childHierarchy')
				.subscribe(
					(response) => observer.next(response),
					(exception) => observer.error(exception)
				);
		});
		return observable;
	}

	getCodeParents(id : string):Observable<CodeSetValue[]> {
		var vm = this;

		var observable = Observable.create(observer => {
			vm.httpGet('/api/ekb/hierarchy/' + id + '/parentHierarchy')
				.subscribe(
					(response) => observer.next(response),
					(exception) => observer.error(exception)
				);
		});
		return observable;
	}

	getPreferredTerm(id : string):Observable<Concept> {
		return this.httpGet('/api/ekb/concepts/' + id);
	}
}
