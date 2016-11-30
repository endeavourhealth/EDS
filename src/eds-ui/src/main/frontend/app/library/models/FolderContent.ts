import {ItemType} from "./ItemType";

export class FolderItem {
	uuid:string;
	type:ItemType;
	name:string;
	description:string;
	children:FolderItem[] = [];
	lastModified:number;
	lastRun:number; //only applicable when showing reports
	isScheduled:boolean; //only applicable when showing reports
}
