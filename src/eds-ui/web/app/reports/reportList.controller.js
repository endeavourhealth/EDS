/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var reports;
    (function (reports) {
        var FolderType = app.models.FolderType;
        var QueueReportController = app.dialogs.QueueReportController;
        var LibraryItemFolderModuleBase = app.blocks.LibraryItemFolderModuleBase;
        var ItemType = app.models.ItemType;
        'use strict';
        var ReportListController = (function (_super) {
            __extends(ReportListController, _super);
            function ReportListController(reportService, folderService, logger, moduleStateService, $scope, $modal, $state) {
                _super.call(this, logger, $modal, folderService, FolderType.Report);
                this.reportService = reportService;
                this.folderService = folderService;
                this.logger = logger;
                this.moduleStateService = moduleStateService;
                this.$scope = $scope;
                this.$modal = $modal;
                this.$state = $state;
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
            ReportListController.prototype.selectNode = function (node) {
                _super.prototype.selectNode.call(this, node);
                this.selectedReport = null;
                this.selectedReportSchedules = null;
                this.selectedSchedule = null;
                this.selectedScheduleResults = null;
            };
            ReportListController.prototype.actionItem = function (uuid, type, action) {
                this.saveState();
                switch (type) {
                    case ItemType.Report:
                        this.$state.go('app.reportAction', { itemUuid: uuid, itemAction: action });
                        break;
                    default:
                        this.logger.error('Invalid item type', type, 'Item ' + action);
                        break;
                }
            };
            ReportListController.prototype.run = function (item) {
                var vm = this;
                QueueReportController.open(vm.$modal, item.uuid, item.name)
                    .result.then(function (result) {
                    vm.scheduleReport(result);
                });
            };
            ReportListController.prototype.scheduleReport = function (requestParameters) {
                var vm = this;
                vm.reportService.scheduleReport(requestParameters)
                    .then(function (result) {
                    vm.logger.success('Report queued', result, 'Run report');
                    if (requestParameters.reportUuid === vm.selectedReport.uuid) {
                        vm.selectFolderItem(vm.selectedReport);
                    }
                })
                    .catch(function (error) {
                    vm.logger.error('Error queueing report', error, 'Run report');
                });
            };
            ReportListController.prototype.selectFolderItem = function (item) {
                var vm = this;
                vm.selectedReport = item;
                vm.selectedReportSchedules = null;
                vm.selectedSchedule = null;
                vm.selectedScheduleResults = null;
                vm.reportService.getReportSchedules(item.uuid, 5)
                    .then(function (result) {
                    vm.selectedReportSchedules = result;
                });
            };
            ReportListController.prototype.selectSchedule = function (schedule) {
                var vm = this;
                vm.selectedSchedule = schedule;
                vm.selectedScheduleResults = null;
                vm.reportService.getScheduleResults(schedule.uuid)
                    .then(function (results) {
                    vm.selectedScheduleResults = results;
                });
            };
            ReportListController.prototype.deleteItem = function (item) {
                var vm = this;
                vm.reportService.deleteReport(item.uuid)
                    .then(function (result) {
                    var i = vm.itemSummaryList.contents.indexOf(item);
                    vm.itemSummaryList.contents.splice(i, 1);
                    vm.logger.success('Report deleted', result, 'Delete report');
                })
                    .catch(function (error) {
                    vm.logger.error('Error deleting report', error, 'Delete report');
                });
            };
            ReportListController.prototype.saveState = function () {
                var state = {
                    selectedNode: this.selectedNode,
                    treeData: this.treeData,
                    selectedReport: this.selectedReport,
                    selectedReportSchedules: this.selectedReportSchedules,
                    selectedSchedule: this.selectedSchedule,
                    selectedScheduleResults: this.selectedScheduleResults
                };
                this.moduleStateService.setState('reportList', state);
            };
            ReportListController.prototype.cutItem = function (item) {
                var vm = this;
                vm.reportService.getReport(item.uuid)
                    .then(function (report) {
                    vm.moduleStateService.setState('reportClipboard', report);
                    vm.logger.success('Item cut to clipboard', report, 'Cut');
                })
                    .catch(function (error) {
                    vm.logger.error('Error cutting to clipboard', error, 'Cut');
                });
            };
            ReportListController.prototype.copyItem = function (item) {
                var vm = this;
                vm.reportService.getReport(item.uuid)
                    .then(function (report) {
                    vm.moduleStateService.setState('reportClipboard', report);
                    report.uuid = null; // Force save as new
                    vm.logger.success('Item copied to clipboard', report, 'Copy');
                })
                    .catch(function (error) {
                    vm.logger.error('Error copying to clipboard', error, 'Copy');
                });
            };
            ReportListController.prototype.pasteItem = function (node) {
                var vm = this;
                var report = vm.moduleStateService.getState('reportClipboard');
                if (report) {
                    report.folderUuid = node.uuid;
                    vm.reportService.saveReport(report)
                        .then(function (result) {
                        vm.logger.success('Item pasted to folder', report, 'Paste');
                        // reload folder if still selection
                        if (vm.selectedNode.uuid === node.uuid) {
                            vm.selectedNode = null;
                            vm.selectNode(node);
                        }
                    })
                        .catch(function (error) {
                        vm.logger.error('Error pasting clipboard', error, 'Paste');
                    });
                }
            };
            ReportListController.$inject = ['ReportService', 'FolderService', 'LoggerService', 'ModuleStateService', '$scope', '$uibModal',
                '$state'];
            return ReportListController;
        })(LibraryItemFolderModuleBase);
        reports.ReportListController = ReportListController;
        angular
            .module('app.reports')
            .controller('ReportListController', ReportListController);
    })(reports = app.reports || (app.reports = {}));
})(app || (app = {}));
//# sourceMappingURL=reportList.controller.js.map