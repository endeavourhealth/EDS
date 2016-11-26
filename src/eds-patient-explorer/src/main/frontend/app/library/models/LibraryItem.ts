import {System} from "../../systems/models/System";
import {Protocol} from "../../protocols/models/Protocol";
import {DataSet} from "../../dataSets/models/Dataset";
import {CodeSet} from "../../codeSet/models/CodeSet";
import {Query} from "../../queries/models/Query";

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
