/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />

module app.reports {
	import FolderNode = app.models.FolderNode;
	import ItemSummaryList = app.models.ItemSummaryList;
	import ILoggerService = app.blocks.ILoggerService;
	import FolderContent = app.models.FolderItem;
	import itemTypeIdToString = app.models.itemTypeIdToString;
	import LibraryController = app.library.LibraryController;
	import ICallbacks = AngularUITree.ICallbacks;
	import IEventInfo = AngularUITree.IEventInfo;
	import FolderItem = app.models.FolderItem;
	import ReportNode = app.models.ReportNode;
	import ItemType = app.models.ItemType;
	import Report = app.models.Report;
	import ReportItem = app.models.ReportItem;
	import UuidNameKVP = app.models.UuidNameKVP;
	import IScope = angular.IScope;
	import IWindowService = angular.IWindowService;
	import IFolderService = app.core.IFolderService;
	import IReportService = app.core.IReportService;
	import IAcceptCallback = AngularUITree.IAcceptCallback;
	import ITreeNodeScope = AngularUITree.ITreeNodeScope;
	'use strict';

	class ReportController {
		treeData : FolderNode[];
		selectedNode : FolderNode = null;
		itemSummaryList : ItemSummaryList;
		report : Report;
		reportContent : ReportNode[];
		contentTreeCallbackOptions : ICallbacks;
		structureTreeCallbackOptions : ICallbacks;
		dataSourceMap : any;

		static $inject = ['ReportService', 'FolderService', 'LoggerService', '$stateParams', 'AdminService', '$window'];

		constructor(
			protected reportService:IReportService,
			protected folderService:IFolderService,
			protected logger : ILoggerService,
			protected $stateParams : {itemAction : string, itemUuid : string},
			protected adminService : IAdminService,
			protected $window : IWindowService) {
			this.contentTreeCallbackOptions = {dropped: this.contentTreeDroppedCallback, accept: null, dragStart: null};
			this.structureTreeCallbackOptions = {dropped: null, accept: this.structureTreeAcceptCallback, dragStart: null};

			this.getLibraryRootFolders();
			this.performAction($stateParams.itemAction, $stateParams.itemUuid);
		}

		// General report methods
		performAction(action:string, itemUuid:string) {
			switch (action) {
				case 'add':
					this.createReport(itemUuid);
					break;
				case 'edit':
					this.getReport(itemUuid);
					break;
			}
		}

		createReport(folderUuid:string) {
			// Initialize blank report
			this.report = {
				uuid: '',
				name: 'New report',
				description: '',
				folderUuid: folderUuid,
				reportItem: []
			};
			this.reportContent = [];
		}

		getReport(reportUuid:string) {
			var vm = this;
			vm.reportService.getReport(reportUuid)
				.then(function (data) {
					vm.report = data;
					vm.reportContent = [];
					vm.reportService.getContentNamesForReportLibraryItem(reportUuid)
						.then(function(data) {
							vm.dataSourceMap = UuidNameKVP.toAssociativeArray(data.contents);
							vm.populateTreeFromReportLists(vm.report.reportItem, vm.reportContent);
					})
					.catch(function(data) {
						vm.logger.error('Error loading report item names', data, 'Error');
					});
				})
				.catch(function(data) {
					vm.logger.error('Error loading report', data, 'Error');
				});
		}

		populateTreeFromReportLists(reportItems : ReportItem[], nodeList : ReportNode[]) {
			var vm = this;
			if (reportItems == null) { reportItems = []; }

			for (var i = 0; i < reportItems.length; i++) {
				var reportItem:ReportItem = reportItems[i];

				var uuid:string = null;
				var type:ItemType = null;

				if (reportItem.queryLibraryItemUuid && reportItem.queryLibraryItemUuid !== '') {
					uuid = reportItem.queryLibraryItemUuid;
					type = ItemType.Query;
				} else if (reportItem.listReportLibraryItemUuid && reportItem.listReportLibraryItemUuid !== '') {
					uuid = reportItem.listReportLibraryItemUuid;
					type = ItemType.ListOutput;
				}

				if (uuid != null) {
					var reportNode:ReportNode = {
						uuid : uuid,
						name : vm.dataSourceMap[uuid],
						type : type,
						children : []
					};

					nodeList.push(reportNode);
					if (reportItem.reportItem && reportItem.reportItem.length > 0) {
						vm.populateTreeFromReportLists(reportItem.reportItem, reportNode.children);
					}
				}
			}
		}

		save(close : boolean) {
			var vm = this;
			vm.report.reportItem = [];
			vm.populateReportListsFromTree(vm.report.reportItem, vm.reportContent);

			vm.reportService.saveReport(vm.report)
				.then(function (data:Report) {
					vm.report.uuid = data.uuid;
					vm.adminService.clearPendingChanges();
					vm.logger.success('Report saved', vm.report, 'Saved');
					if (close) { vm.$window.history.back(); }
				})
				.catch(function(data) {
					vm.logger.error('Error saving report', data, 'Error');
				});
		}

		close() {
			this.adminService.clearPendingChanges();
			this.$window.history.back();
		}

		populateReportListsFromTree(reportItems : ReportItem[], nodes : ReportNode[]) {
			for (var i = 0; i < nodes.length; i++) {
				var reportItem : ReportItem = {
					queryLibraryItemUuid : null,
					listReportLibraryItemUuid : null,
					reportItem : []
				};

				switch (nodes[i].type) {
					case ItemType.Query:
						reportItem.queryLibraryItemUuid = nodes[i].uuid;
						break;
					case ItemType.ListOutput:
						reportItem.listReportLibraryItemUuid = nodes[i].uuid;
						break;
				}
				reportItems.push(reportItem);
				if (nodes[i].children && nodes[i].children.length > 0) {
					this.populateReportListsFromTree(reportItem.reportItem, nodes[i].children);
				}
			}
		}

		// Library tree methods
		getLibraryRootFolders() {
			var vm = this;
			vm.folderService.getFolders(1, null)
				.then(function (data) {
					vm.treeData = data.folders;
				});
		}

		selectNode(node : FolderNode) {
			if (node === this.selectedNode) { return; }
			var vm = this;

			vm.selectedNode = node;
			node.loading = true;
			vm.folderService.getFolderContents(node.uuid)
				.then(function(data) {
					vm.itemSummaryList = data;
					// filter content by those allowed in reports
					if (vm.itemSummaryList.contents) {
						vm.itemSummaryList.contents = vm.itemSummaryList.contents.filter(vm.validReportItemType);
					}
					node.loading = false;
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
						node.loading = false;
					});
			}
		}

		// Report structure methods
		remove(scope:any) {
			scope.remove();
		}

		// Library folder content methods
		validReportItemType(input:FolderContent):boolean {
			switch (input.type) {
				case ItemType.Query:
				case ItemType.ListOutput:
					return true;
				default:
					return false;
			}
		};

		contentTreeDroppedCallback(eventInfo: IEventInfo) {
			// Convert clone model to report node
			eventInfo.source.cloneModel.children = [];
		}

		structureTreeAcceptCallback(source: any, destination: any, destinationIndex: number): boolean {
			// Check for same type at same level
			if (!destination.$modelValue.every(
					(sibling : ReportNode) => {return sibling.uuid !== source.$modelValue.uuid; })
			) {
				return false;
			}

			// Check for self as a parent
			var parent : any = destination.$nodeScope;

			while (parent) {
				if (parent.$modelValue.uuid === source.$modelValue.uuid) {
					return false;
				}
				parent = parent.$nodeScope;
			}

			return true;
		}

	}

	angular
		.module('app.reports')
		.controller('ReportController', ReportController);
}
