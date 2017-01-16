import {CountReport} from "../../countReport/models/CountReport";

export class LibraryItem {
	uuid:string;
	name:string;
	description:string;
	folderUuid:string;
	countReport:CountReport;
}
