import {System} from "../../system/models/System";
import {Protocol} from "../../protocol/models/Protocol";
import {DataSet} from "../../dataset/models/Dataset";
import {CodeSet} from "../../tests/models/CodeSet";
import {Query} from "../../query/models/Query";

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
