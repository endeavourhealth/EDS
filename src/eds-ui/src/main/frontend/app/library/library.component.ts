import {Component} from "@angular/core";
import {StateService} from "ui-router-ng2";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

import {LibraryService} from "./library.service";
import {FolderService} from "../folder/folder.service";
import {LoggerService} from "../common/logger.service";
import {ModuleStateService} from "../common/moduleState.service";
import {ItemType} from "../models/ItemType";
import {FolderItem} from "../models/FolderContent";
import {LibraryItem} from "../models/LibraryItem";
import {FolderNode} from "../models/FolderNode";
import {ItemSummaryList} from "../models/ItemSummaryList";

@Component({
	template : require('./library.html')
})
export class LibraryComponent {
	selectedFolder : FolderNode;
	itemSummaryList : ItemSummaryList;

	constructor(
		protected libraryService:LibraryService,
		protected folderService:FolderService,
		protected log:LoggerService,
		protected moduleStateService : ModuleStateService,
		protected $modal : NgbModal,
		protected $state : StateService) {
	}

	folderChanged($event) {
		this.selectedFolder = $event.selectedFolder;
		this.refresh();
	}

	refresh() {
		var vm = this;
		vm.folderService.getFolderContents(vm.selectedFolder.uuid)
			.subscribe(
				(data) => {
					vm.itemSummaryList = data;
					vm.selectedFolder.loading = false;
				});
	}

	getSummaryList() {
		// Todo : Implement sorting
		return (this.itemSummaryList) ? this.itemSummaryList.contents : null;
	}

	saveFolderState() {
		var state = {
			selectedNode : this.selectedFolder
		};
		this.moduleStateService.setState('protocolFolder', state);
	}

	actionItem($event) {
		this.saveFolderState();
		switch ($event.type) {
			case ItemType.System:
				this.$state.go('systemEdit', {itemUuid: $event.uuid, itemAction: $event.action});
				break;
			case ItemType.Protocol:
				this.$state.go('protocolEdit', {itemUuid: $event.uuid, itemAction: $event.action});
				break;
			case ItemType.Query:
				this.$state.go('queryEdit', {itemUuid: $event.uuid, itemAction: $event.action});
				break;
			case ItemType.DataSet:
				this.$state.go('dataSetEdit', {itemUuid: $event.uuid, itemAction: $event.action});
				break;
			case ItemType.CodeSet:
				this.$state.go('codeSetEdit', {itemUuid: $event.uuid, itemAction: $event.action});
				break;
			default:
				this.log.error('Invalid item type', $event.type, 'Item ' + $event.action);
				break;
		}
	}

	deleteItem(item : FolderItem) {
		var vm = this;
		vm.libraryService.deleteLibraryItem(item.uuid)
			.subscribe(
				(result) => {
					var i = vm.itemSummaryList.contents.indexOf(item);
					vm.itemSummaryList.contents.splice(i, 1);
					vm.log.success('Library item deleted', result, 'Delete item');
				},
				(error) => vm.log.error('Error deleting library item', error, 'Delete item')
			);
	}

	cutItem(item : FolderItem) {
		var vm = this;
		vm.libraryService.getLibraryItem(item.uuid)
			.subscribe(
				(libraryItem) => {
					vm.moduleStateService.setState('libraryClipboard', libraryItem);
					vm.log.success('Item cut to clipboard', libraryItem, 'Cut');
				},
				(error) => vm.log.error('Error cutting to clipboard', error, 'Cut')
			);
	}

	copyItem(item : FolderItem) {
		var vm = this;
		vm.libraryService.getLibraryItem(item.uuid)
			.subscribe(
				(libraryItem) => {
					vm.moduleStateService.setState('libraryClipboard', libraryItem);
					libraryItem.uuid = null;		// Force save as new
					vm.log.success('Item copied to clipboard', libraryItem, 'Copy');
				},
				(error) => vm.log.error('Error copying to clipboard', error, 'Copy')
			);
	}

	pasteItem(node : FolderNode) {
		var vm = this;
		var libraryItem : LibraryItem = vm.moduleStateService.getState('libraryClipboard') as LibraryItem;
		if (libraryItem) {
			libraryItem.folderUuid = node.uuid;
			vm.libraryService.saveLibraryItem(libraryItem)
				.subscribe(
					(result) => {
						vm.log.success('Item pasted to folder', libraryItem, 'Paste');
						// reload folder if still selection
						if (vm.selectedFolder.uuid === node.uuid) {
							vm.refresh();
						}
					},
					(error) => vm.log.error('Error pasting clipboard', error, 'Paste')
				);
		}
	}

}
