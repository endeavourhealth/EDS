import {Folder} from "./Folder";

export class FolderNode extends Folder {
	isExpanded:boolean;
	loading:boolean;
	nodes:FolderNode[];
}
