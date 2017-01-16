import {Observable} from "rxjs";
import {CodeSetValue} from "./models/CodeSetValue";
import {Concept} from "./models/Concept";

export abstract class CodingService {
	abstract searchCodes(searchData : string):Observable<CodeSetValue[]>;
	abstract getCodeChildren(code : string):Observable<CodeSetValue[]>;
	abstract getCodeParents(code : string):Observable<CodeSetValue[]>;
	abstract getPreferredTerm(id : string):Observable<Concept>;
}
