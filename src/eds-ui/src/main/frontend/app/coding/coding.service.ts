import {Observable} from "rxjs";
import {CodeSetValue} from "./models/CodeSetValue";
import {Concept} from "./models/Concept";

export interface ICodingService {
	searchCodes(searchData : string):Observable<CodeSetValue[]>;
	getCodeChildren(code : string):Observable<CodeSetValue[]>;
	getCodeParents(code : string):Observable<CodeSetValue[]>;
	getPreferredTerm(id : string):Observable<Concept>;
}
