import {LoggerService} from "../common/logger.service";
import {FolderNode} from "./models/FolderNode";
import {Folder} from "./models/Folder";
import {FolderService} from "./folder.service";
import {InputBoxDialog} from "../dialogs/inputBox/inputBox.dialog";
import {MessageBoxDialog} from "../dialogs/messageBox/messageBox.dialog";
import {FolderType} from "./models/FolderType";
import {ItemType} from "./models/ItemType";
import {ModuleStateService} from "../common/moduleState.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Component, OnInit, ViewChild, EventEmitter, Output, Input} from "@angular/core";
import {ITreeOptions, TreeNode, TreeComponent} from "angular2-tree-component";
import {Observable} from "rxjs";
import {ITreeNode} from "angular2-tree-component/dist/defs/api";

@Component({
	selector : 'library-folder',
	template : require('./folder.html'),
})
export class FolderComponent implements OnInit {
	@Input() folderType = FolderType.Library;
	@Input() showMenu = false;
	@Output() selectionChange = new EventEmitter();
	@Output() itemActioned = new EventEmitter();
	@ViewChild(TreeComponent) tree: TreeComponent;

	public onSelected : Function;
	public onActionItem : Function;
	public id : string = 'libraryFolder';

	selectedNode : FolderNode;
	treeData : FolderNode[];
	options : ITreeOptions;

	constructor(
		protected logger : LoggerService,
		protected $modal : NgbModal,
		protected moduleStateService : ModuleStateService,
		protected folderService : FolderService) {
		this.options = {
			displayField : 'folderName',
			childrenField : 'nodes',
			idField : 'uuid',
			isExpandedField : 'isExpanded',
			getChildren : (node) => { this.getChildren(node)}
		}
	}

	ngOnInit() {
		this.getRootFolders();
	}

	getRootFolders() {
		var vm = this;
		vm.folderService.getFolders(vm.folderType, null)
			.subscribe(
				(rootData) => {
					vm.folderService.getFolders(1, rootData.folders[0].uuid)
						.subscribe(
							(childData => {
								vm.treeData = rootData.folders;

								if (vm.treeData && vm.treeData.length > 0) {
									// Set folder type (not retrieved by API)
									vm.treeData.forEach((item) => {
										item.folderType = vm.folderType;
									});
								}
								// Set parent folder (not retrieved by API)
								childData.folders.forEach((item) => {
									item.parentFolderUuid = rootData.folders[0].uuid;
								});
								vm.treeData[0].nodes = childData.folders;
							}),
							(error) => vm.logger.error('Error loading root children', error)
					)
				},
				(error) => vm.logger.error('Error loading root', error)
			);
	}

	treeInitialized() {
		// Expand root if not already done
		this.tree.treeModel.getFirstRoot().expand();

		// Select previous selection if present
		var state = this.moduleStateService.getState(this.id);
		if (state) {
			this.tree.treeModel.setActiveNode(this.tree.treeModel.getNodeById(state.selectedNode.uuid), true);
		}
	}


	saveState() {
		var state = {
			selectedNode : this.selectedNode
		};
		this.moduleStateService.setState(this.id, state);
	}

	getChildren(node : ITreeNode) {
		var vm = this;
		let observable = Observable.create(observer => {
			vm.folderService.getFolders(1, node.id)
				.subscribe(
					(data) => {
						// Set parent folder (not retrieved by API)
						data.folders.forEach((item) => {
							item.parentFolderUuid = node.id;
						});
						observer.next(data.folders);
						node.data.isExpanded = true;
						node.data.nodes = data.folders;
						vm.tree.treeModel.update();
					});
		});
		return observable.toPromise();
	}

	selectNode(node : FolderNode) {
		if (node === this.selectedNode) { return; }
		var vm = this;
		vm.selectedNode = node;
		vm.saveState()
		node.loading = true;
		vm.selectionChange.emit({selectedFolder: node});
	}

	actionItem(uuid : string, type : ItemType, action : string) {
		var vm = this;
		vm.saveState();
		vm.itemActioned.emit({uuid : uuid, type : type, action : action});
	}
}
