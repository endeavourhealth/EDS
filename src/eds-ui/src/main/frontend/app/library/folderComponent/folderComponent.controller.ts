import IModalService = angular.ui.bootstrap.IModalService;

import {ILoggerService} from "../../blocks/logger.service";
import {FolderNode} from "../../models/FolderNode";
import {Folder} from "../../models/Folder";
import {IFolderService} from "../../core/folder.service";
import {InputBoxController} from "../../dialogs/inputBox/inputBox.controller";
import {MessageBoxController} from "../../dialogs/messageBox/messageBox.controller";
import {FolderType} from "../../models/FolderType";
import {ItemType} from "../../models/ItemType";
import {IModuleStateService} from "../../core/moduleState.service";

export class FolderComponentController {
	public onSelected : Function;
	public onActionItem : Function;
	public id : string = 'libraryFolder';

	folderType : FolderType = FolderType.Library;
	selectedNode : FolderNode;
	treeData : FolderNode[];

	static $inject = ['LoggerService', '$uibModal', 'ModuleStateService', 'FolderService'];

	constructor(
		protected logger : ILoggerService,
		protected $modal : IModalService,
		protected moduleStateService : IModuleStateService,
		protected folderService : IFolderService) {
	}

	$onInit() {
		this.getRootFolders();
		var state = this.moduleStateService.getState(this.id);
		if (state) {
			this.selectNode(state.selectedNode);
		}
	}


	saveState() {
		var state = {
			selectedNode : this.selectedNode
		};
		this.moduleStateService.setState(this.id, state);
	}


	getRootFolders() {
		var vm = this;
		vm.folderService.getFolders(vm.folderType, null)
			.then(function (data) {
				vm.treeData = data.folders;

				if (vm.treeData && vm.treeData.length > 0) {
					// Set folder type (not retrieved by API)
					vm.treeData.forEach((item) => { item.folderType = vm.folderType; } );
					// Expand top level by default
					vm.toggleExpansion(vm.treeData[0]);
				}
			})
			.catch(function (error) {
				vm.logger.error(error);
			});
	}

	toggleExpansion(node : FolderNode) {
		if (!node.hasChildren) { return; }

		node.isExpanded = !node.isExpanded;

		if (node.isExpanded && (node.nodes == null || node.nodes.length === 0)) {
			var vm = this;
			var folderId = node.uuid;
			node.loading = true;
			this.folderService.getFolders(1, folderId)
				.then(function (data) {
					node.nodes = data.folders;
					// Set parent folder (not retrieved by API)
					node.nodes.forEach((item) => { item.parentFolderUuid = node.uuid; } );
					node.loading = false;
				});
		}
	}

	selectNode(node : FolderNode) {
		if (node === this.selectedNode) { return; }
		var vm = this;

		vm.selectedNode = node;
		node.loading = true;
		vm.onSelected({selectedFolder: node});
	}

	actionItem(uuid : string, type : ItemType, action : string) {
		var vm = this;
		vm.saveState();
		vm.onActionItem({uuid : uuid, type : type, action : action});
	}

	addChildFolder(node : FolderNode) {
		var vm = this;
		InputBoxController.open(vm.$modal, 'New Folder', 'Enter new folder name', 'New folder')
			.result.then(function(result : string) {
			var folder : Folder = {
				uuid : null,
				folderName : result,
				folderType : vm.folderType,
				parentFolderUuid : node.uuid,
				contentCount : 0,
				hasChildren : false
			};
			vm.folderService.saveFolder(folder)
				.then(function(response) {
					vm.logger.success('Folder created', response, 'New folder');
					node.isExpanded = false;
					node.hasChildren = true;
					node.nodes = null;
					vm.toggleExpansion(node);
				})
				.catch(function(error){
					vm.logger.error('Error creating folder', error, 'New folder');
				});
		});
	}

	renameFolder(scope : any) {
		var vm = this;
		var folderNode : FolderNode = scope.$modelValue;
		InputBoxController.open(vm.$modal,
			'Rename folder', 'Enter new name for ' + folderNode.folderName, folderNode.folderName)
			.result.then(function(newName : string) {
			var oldName = folderNode.folderName;
			folderNode.folderName = newName;
			vm.folderService.saveFolder(folderNode)
				.then(function (response) {
					vm.logger.success('Folder renamed to ' + newName, response, 'Rename folder');
				})
				.catch(function (error) {
					folderNode.folderName = oldName;
					vm.logger.error('Error renaming folder', error, 'Rename folder');
				});
		});
	}

	deleteFolder(scope : any) {
		var vm = this;
		var folderNode : FolderNode = scope.$modelValue;
		MessageBoxController.open(vm.$modal,
			'Delete folder', 'Are you sure you want to delete folder ' + folderNode.folderName + '?', 'Yes', 'No')
			.result.then(function() {
			vm.folderService.deleteFolder(folderNode)
				.then(function (response) {
					scope.remove();
					vm.logger.success('Folder deleted', response, 'Delete folder');
				})
				.catch(function (error) {
					vm.logger.error('Error deleting folder', error, 'Delete folder');
				});
		});
	}
}
