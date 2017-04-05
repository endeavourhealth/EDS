import {System} from "../../system/models/System";
import {Protocol} from "../../protocol/models/Protocol";
import {DataSet} from "../../dataSet/models/Dataset";
import {CodeSet} from "../../tests/models/CodeSet";
import {Query} from "../../query/models/Query";
import {CountReport} from "../../countReport/models/CountReport";
import {LibraryItem} from "eds-common-js/dist/library/models/LibraryItem";

export class EdsLibraryItem  extends LibraryItem {
	query:Query;
	// resource:Resource;
	// test:Test;
	codeSet:CodeSet;
	dataSet:DataSet;
	protocol:Protocol;
	system:System;
	countReport:CountReport;
}
