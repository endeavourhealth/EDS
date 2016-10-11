import IScope = angular.IScope;
import IModalService = angular.ui.bootstrap.IModalService;
import IStateService = angular.ui.IStateService;

import {ILibraryService} from "../core/library.service";
import {IFolderService} from "../core/folder.service";
import {ILoggerService} from "../blocks/logger.service";
import {IModuleStateService} from "../core/moduleState.service";
import {ItemType} from "../models/ItemType";
import {FolderItem} from "../models/FolderContent";
import {LibraryItem} from "../models/LibraryItem";
import {FolderNode} from "../models/FolderNode";
import {ItemSummaryList} from "../models/ItemSummaryList";

export class LibraryController {
	selectedFolder : FolderNode;
	itemSummaryList : ItemSummaryList;

	static $inject = ['LibraryService', 'FolderService', 'LoggerService', 'ModuleStateService', '$scope', '$uibModal',
		'$state'];

	constructor(
		protected libraryService:ILibraryService,
		protected folderService:IFolderService,
		protected logger:ILoggerService,
		protected moduleStateService : IModuleStateService,
		protected $scope : IScope,
		protected $modal : IModalService,
		protected $state : IStateService) {
	}

	folderChanged(node : FolderNode) {
		this.selectedFolder = node;
		this.refresh();
	}

	refresh() {
		var vm = this;
		vm.folderService.getFolderContents(vm.selectedFolder.uuid)
			.then(function(data) {
				vm.itemSummaryList = data;
				vm.selectedFolder.loading = false;
			});
	}

	saveFolderState() {
		var state = {
			selectedNode : this.selectedFolder
		};
		this.moduleStateService.setState('protocolFolder', state);
	}

	actionItem(uuid : string, type : ItemType, action : string) {
		this.saveFolderState();
		switch (type) {
			case ItemType.System:
				this.$state.go('app.systemAction', {itemUuid: uuid, itemAction: action});
				break;
			case ItemType.Protocol:
				this.$state.go('app.protocolAction', {itemUuid: uuid, itemAction: action});
				break;
			case ItemType.Query:
				this.$state.go('app.queryAction', {itemUuid: uuid, itemAction: action});
				break;
			case ItemType.DataSet:
				this.$state.go('app.dataSetAction', {itemUuid: uuid, itemAction: action});
				break;
			case ItemType.CodeSet:
				this.$state.go('app.codeSetAction', {itemUuid: uuid, itemAction: action});
				break;
			default:
				this.logger.error('Invalid item type', type, 'Item ' + action);
				break;
		}
	}

	deleteItem(item : FolderItem) {
		var vm = this;
		vm.libraryService.deleteLibraryItem(item.uuid)
			.then(function(result) {
				var i = vm.itemSummaryList.contents.indexOf(item);
				vm.itemSummaryList.contents.splice(i, 1);
				vm.logger.success('Library item deleted', result, 'Delete item');
			})
			.catch(function(error) {
				vm.logger.error('Error deleting library item', error, 'Delete item');
			});
	}

	cutItem(item : FolderItem) {
		var vm = this;
		vm.libraryService.getLibraryItem(item.uuid)
			.then(function(libraryItem : LibraryItem) {
				vm.moduleStateService.setState('libraryClipboard', libraryItem);
				vm.logger.success('Item cut to clipboard', libraryItem, 'Cut');
			})
			.catch(function(error) {
				vm.logger.error('Error cutting to clipboard', error, 'Cut');
			});
	}

	copyItem(item : FolderItem) {
		var vm = this;
		vm.libraryService.getLibraryItem(item.uuid)
			.then(function(libraryItem : LibraryItem) {
				vm.moduleStateService.setState('libraryClipboard', libraryItem);
				libraryItem.uuid = null;		// Force save as new
				vm.logger.success('Item copied to clipboard', libraryItem, 'Copy');
			})
			.catch(function(error) {
				vm.logger.error('Error copying to clipboard', error, 'Copy');
			});
	}

	pasteItem(node : FolderNode) {
		var vm = this;
		var libraryItem : LibraryItem = vm.moduleStateService.getState('libraryClipboard') as LibraryItem;
		if (libraryItem) {
			libraryItem.folderUuid = node.uuid;
			vm.libraryService.saveLibraryItem(libraryItem)
				.then(function(result) {
					vm.logger.success('Item pasted to folder', libraryItem, 'Paste');
					// reload folder if still selection
					if (vm.selectedFolder.uuid === node.uuid) {
						vm.refresh();
					}
				})
				.catch(function(error){
					vm.logger.error('Error pasting clipboard', error, 'Paste');
				});
		}
	}

}
