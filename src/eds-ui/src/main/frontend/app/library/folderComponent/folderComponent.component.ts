import {FolderComponentController} from "./folderComponent.controller";

export class FolderComponent implements ng.IComponentOptions {
	public bindings : any;
	public controller : any;
	public template : any;
	public transclude : any;

	constructor () {
		this.bindings = {
			onSelected: "&",
			onActionItem: "&"
		};
		this.template = require('./libraryItemFolder.html');
		this.controller = FolderComponentController;
	}
}