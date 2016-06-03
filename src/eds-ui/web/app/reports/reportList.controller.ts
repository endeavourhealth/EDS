/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />

module app.reports {
	import FolderNode = app.models.FolderNode;
	import ItemSummaryList = app.models.ItemSummaryList;
	import ILoggerService = app.blocks.ILoggerService;
	import FolderContent = app.models.FolderItem;
	import itemTypeIdToString = app.models.itemTypeIdToString;
	import IScope = angular.IScope;
	import FolderItem = app.models.FolderItem;
	import FolderType = app.models.FolderType;
	import InputBoxController = app.dialogs.InputBoxController;
	import MessageBoxController = app.dialogs.MessageBoxController;
	import IModalService = angular.ui.bootstrap.IModalService;
	import Folder = app.models.Folder;
	import QueueReportController = app.dialogs.QueueReportController;
	import RequestParameters = app.models.RequestParameters;
	import LoggerService = app.blocks.LoggerService;
	import IReportService = app.core.IReportService;
	import IFolderService = app.core.IFolderService;
	import ReportSchedule = app.models.ReportSchedule;
	import ReportResult = app.models.ReportResult;
	import LibraryItemFolderModuleBase = app.blocks.LibraryItemFolderModuleBase;
	import IModuleStateService = app.core.IModuleStateService;
	import ItemType = app.models.ItemType;
	import Report = app.models.Report;
	'use strict';

	export class ReportListController extends LibraryItemFolderModuleBase {
		selectedReport : FolderItem;
		selectedReportSchedules : ReportSchedule[];
		selectedSchedule : ReportSchedule;
		selectedScheduleResults : ReportResult;

		static $inject = ['ReportService', 'FolderService', 'LoggerService', 'ModuleStateService', '$scope', '$uibModal',
			'$state'];

		constructor(
			protected reportService:IReportService,
			protected folderService : IFolderService,
			protected logger : ILoggerService,
			protected moduleStateService : IModuleStateService,
			protected $scope : IScope,
			protected $modal : IModalService,
			protected $state : IStateService) {
			super(logger, $modal, folderService, FolderType.Report);

			var state = moduleStateService.getState('reportList');
			if (state) {
				this.treeData = state.treeData;
				this.selectNode(state.selectedNode);

				this.selectedReport = state.selectedReport;
				this.selectedReportSchedules = state.selectedReportSchedules;
				this.selectedSchedule = state.selectedSchedule;
				this.selectedScheduleResults = state.selectedScheduleResults;
			}
		}

		selectNode(node : FolderNode) {
			super.selectNode(node);
			this.selectedReport = null;
			this.selectedReportSchedules = null;
			this.selectedSchedule = null;
			this.selectedScheduleResults = null;
		}

		actionItem(uuid : string, type : ItemType, action : string) {
			this.saveState();
			switch (type) {
				case ItemType.Report:
					this.$state.go('app.reportAction', {itemUuid: uuid, itemAction: action});
					break;
				default:
					this.logger.error('Invalid item type', type, 'Item ' + action);
					break;
			}
		}

		run(item : FolderItem) {
			var vm = this;
			QueueReportController.open(vm.$modal, item.uuid, item.name)
				.result.then(function(result : RequestParameters) {
					vm.scheduleReport(result);
			});
		}

		scheduleReport(requestParameters : RequestParameters) {
			var vm = this;
			vm.reportService.scheduleReport(requestParameters)
				.then(function(result) {
					vm.logger.success('Report queued', result, 'Run report');
					if (requestParameters.reportUuid === vm.selectedReport.uuid) {
						vm.selectFolderItem(vm.selectedReport);
					}
				})
				.catch(function(error) {
					vm.logger.error('Error queueing report', error, 'Run report');
				});
		}

		selectFolderItem(item : FolderItem) {
			var vm = this;
			vm.selectedReport = item;
			vm.selectedReportSchedules = null;
			vm.selectedSchedule = null;
			vm.selectedScheduleResults = null;
			vm.reportService.getReportSchedules(item.uuid, 5)
				.then(function(result) {
					vm.selectedReportSchedules = result;
				});
		}

		selectSchedule(schedule : ReportSchedule) {
			var vm = this;
			vm.selectedSchedule = schedule;
			vm.selectedScheduleResults = null;
			vm.reportService.getScheduleResults(schedule.uuid)
				.then(function(results) {
					vm.selectedScheduleResults = results;
				});
		}

		deleteItem(item : FolderItem) {
			var vm = this;
			vm.reportService.deleteReport(item.uuid)
				.then(function(result) {
					var i = vm.itemSummaryList.contents.indexOf(item);
					vm.itemSummaryList.contents.splice(i, 1);
					vm.logger.success('Report deleted', result, 'Delete report');
				})
				.catch(function(error) {
					vm.logger.error('Error deleting report', error, 'Delete report');
				});
		}

		saveState() {
			var state = {
				selectedNode : this.selectedNode,
				treeData : this.treeData,
				selectedReport : this.selectedReport,
				selectedReportSchedules : this.selectedReportSchedules,
				selectedSchedule : this.selectedSchedule,
				selectedScheduleResults : this.selectedScheduleResults
			};
			this.moduleStateService.setState('reportList', state);
		}

		cutItem(item : FolderItem) {
			var vm = this;
			vm.reportService.getReport(item.uuid)
				.then(function(report : Report) {
					vm.moduleStateService.setState('reportClipboard', report);
					vm.logger.success('Item cut to clipboard', report, 'Cut');
				})
				.catch(function(error) {
					vm.logger.error('Error cutting to clipboard', error, 'Cut');
				});
		}

		copyItem(item : FolderItem) {
			var vm = this;
			vm.reportService.getReport(item.uuid)
				.then(function(report : Report) {
					vm.moduleStateService.setState('reportClipboard', report);
					report.uuid = null;		// Force save as new
					vm.logger.success('Item copied to clipboard', report, 'Copy');
				})
				.catch(function(error) {
					vm.logger.error('Error copying to clipboard', error, 'Copy');
				});
		}

		pasteItem(node : FolderNode) {
			var vm = this;
			var report : Report = vm.moduleStateService.getState('reportClipboard') as Report;
			if (report) {
				report.folderUuid = node.uuid;
				vm.reportService.saveReport(report)
					.then(function(result) {
						vm.logger.success('Item pasted to folder', report, 'Paste');
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
		.module('app.reports')
		.controller('ReportListController', ReportListController);
}
