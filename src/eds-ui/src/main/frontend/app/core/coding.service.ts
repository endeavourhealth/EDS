import {CodeSetValue} from "../models/CodeSetValue";
import {Concept} from "../models/Concept";

export interface ICodingService {
	searchCodes(searchData : string):ng.IPromise<CodeSetValue[]>;
	getCodeChildren(code : string):ng.IPromise<CodeSetValue[]>;
	getCodeParents(code : string):ng.IPromise<CodeSetValue[]>;
	getPreferredTerm(id : string):ng.IPromise<Concept>;
}
