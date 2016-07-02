/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />

module app.library {
	import FolderNode = app.models.FolderNode;
	import ItemSummaryList = app.models.ItemSummaryList;
	import FolderContent = app.models.FolderItem;
	import ILoggerService = app.blocks.ILoggerService;
	import itemTypeIdToString = app.models.itemTypeIdToString;
	import IModalService = angular.ui.bootstrap.IModalService;
	import IModalSettings = angular.ui.bootstrap.IModalSettings;
	import InputBoxController = app.dialogs.InputBoxController;
	import CodePickerController = app.dialogs.CodePickerController;
	import IScope = angular.IScope;
	import TermlexCode = app.models.Code;
	import TermlexCodeSelection = app.models.CodeSelection;
	import Folder = app.models.Folder;
	import FolderType = app.models.FolderType;
	import MessageBoxController = app.dialogs.MessageBoxController;
	import FolderItem = app.models.FolderItem;
	import ItemType = app.models.ItemType;
	import LibraryItem = app.models.LibraryItem;
	import CodeSetValue = app.models.CodeSetValue;
	import IFolderService = app.core.IFolderService;
	import ILibraryService = app.core.ILibraryService;
	import LibraryItemFolderModuleBase = app.blocks.LibraryItemFolderModuleBase;
	import IModuleStateService = app.core.IModuleStateService;
	'use strict';

	export class LibraryController extends LibraryItemFolderModuleBase {
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
			super(logger, $modal, folderService, FolderType.Library);

			var state = moduleStateService.getState('library');
			if (state) {
				this.treeData = state.treeData;
				this.selectNode(state.selectedNode);
			}
		}

		actionItem(uuid : string, type : ItemType, action : string) {
			this.saveState();
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

		saveState() {
			var state = {
				selectedNode : this.selectedNode,
				treeData : this.treeData
			};
			this.moduleStateService.setState('library', state);
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
						if (vm.selectedNode.uuid === node.uuid) {
							vm.selectedNode = null;
							vm.selectNode(node);
						}
					})
					.catch(function(error){
						vm.logger.error('Error pasting clipboard', error, 'Paste');
					});
			}
		}

	}

	angular
		.module('app.library')
		.controller('LibraryController', LibraryController);
}
