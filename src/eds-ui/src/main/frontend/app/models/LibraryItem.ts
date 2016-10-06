import {System} from "./System";
import {Protocol} from "./Protocol";
import {DataSet} from "./Dataset";
import {CodeSet} from "./CodeSet";
import {Query} from "./Query";

export class LibraryItem {
	uuid:string;
	name:string;
	description:string;
	folderUuid:string;
	query:Query;
	// resource:Resource;
	// test:Test;
	codeSet:CodeSet;
	dataSet:DataSet;
	protocol:Protocol;
	system:System;
}
