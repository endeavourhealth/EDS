/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />
var app;
(function (app) {
    var reports;
    (function (reports) {
        var ItemType = app.models.ItemType;
        var UuidNameKVP = app.models.UuidNameKVP;
        'use strict';
        var ReportController = (function () {
            function ReportController(reportService, folderService, logger, $stateParams, adminService, $window) {
                this.reportService = reportService;
                this.folderService = folderService;
                this.logger = logger;
                this.$stateParams = $stateParams;
                this.adminService = adminService;
                this.$window = $window;
                this.selectedNode = null;
                this.contentTreeCallbackOptions = { dropped: this.contentTreeDroppedCallback, accept: null, dragStart: null };
                this.structureTreeCallbackOptions = { dropped: null, accept: this.structureTreeAcceptCallback, dragStart: null };
                this.getLibraryRootFolders();
                this.performAction($stateParams.itemAction, $stateParams.itemUuid);
            }
            // General report methods
            ReportController.prototype.performAction = function (action, itemUuid) {
                switch (action) {
                    case 'add':
                        this.createReport(itemUuid);
                        break;
                    case 'edit':
                        this.getReport(itemUuid);
                        break;
                }
            };
            ReportController.prototype.createReport = function (folderUuid) {
                // Initialize blank report
                this.report = {
                    uuid: '',
                    name: 'New report',
                    description: '',
                    folderUuid: folderUuid,
                    reportItem: []
                };
                this.reportContent = [];
            };
            ReportController.prototype.getReport = function (reportUuid) {
                var vm = this;
                vm.reportService.getReport(reportUuid)
                    .then(function (data) {
                    vm.report = data;
                    vm.reportContent = [];
                    vm.reportService.getContentNamesForReportLibraryItem(reportUuid)
                        .then(function (data) {
                        vm.dataSourceMap = UuidNameKVP.toAssociativeArray(data.contents);
                        vm.populateTreeFromReportLists(vm.report.reportItem, vm.reportContent);
                    })
                        .catch(function (data) {
                        vm.logger.error('Error loading report item names', data, 'Error');
                    });
                })
                    .catch(function (data) {
                    vm.logger.error('Error loading report', data, 'Error');
                });
            };
            ReportController.prototype.populateTreeFromReportLists = function (reportItems, nodeList) {
                var vm = this;
                if (reportItems == null) {
                    reportItems = [];
                }
                for (var i = 0; i < reportItems.length; i++) {
                    var reportItem = reportItems[i];
                    var uuid = null;
                    var type = null;
                    if (reportItem.queryLibraryItemUuid && reportItem.queryLibraryItemUuid !== '') {
                        uuid = reportItem.queryLibraryItemUuid;
                        type = ItemType.Query;
                    }
                    else if (reportItem.listReportLibraryItemUuid && reportItem.listReportLibraryItemUuid !== '') {
                        uuid = reportItem.listReportLibraryItemUuid;
                        type = ItemType.ListOutput;
                    }
                    if (uuid != null) {
                        var reportNode = {
                            uuid: uuid,
                            name: vm.dataSourceMap[uuid],
                            type: type,
                            children: []
                        };
                        nodeList.push(reportNode);
                        if (reportItem.reportItem && reportItem.reportItem.length > 0) {
                            vm.populateTreeFromReportLists(reportItem.reportItem, reportNode.children);
                        }
                    }
                }
            };
            ReportController.prototype.save = function (close) {
                var vm = this;
                vm.report.reportItem = [];
                vm.populateReportListsFromTree(vm.report.reportItem, vm.reportContent);
                vm.reportService.saveReport(vm.report)
                    .then(function (data) {
                    vm.report.uuid = data.uuid;
                    vm.adminService.clearPendingChanges();
                    vm.logger.success('Report saved', vm.report, 'Saved');
                    if (close) {
                        vm.$window.history.back();
                    }
                })
                    .catch(function (data) {
                    vm.logger.error('Error saving report', data, 'Error');
                });
            };
            ReportController.prototype.close = function () {
                this.adminService.clearPendingChanges();
                this.$window.history.back();
            };
            ReportController.prototype.populateReportListsFromTree = function (reportItems, nodes) {
                for (var i = 0; i < nodes.length; i++) {
                    var reportItem = {
                        queryLibraryItemUuid: null,
                        listReportLibraryItemUuid: null,
                        reportItem: []
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
            };
            // Library tree methods
            ReportController.prototype.getLibraryRootFolders = function () {
                var vm = this;
                vm.folderService.getFolders(1, null)
                    .then(function (data) {
                    vm.treeData = data.folders;
                });
            };
            ReportController.prototype.selectNode = function (node) {
                if (node === this.selectedNode) {
                    return;
                }
                var vm = this;
                vm.selectedNode = node;
                node.loading = true;
                vm.folderService.getFolderContents(node.uuid)
                    .then(function (data) {
                    vm.itemSummaryList = data;
                    // filter content by those allowed in reports
                    if (vm.itemSummaryList.contents) {
                        vm.itemSummaryList.contents = vm.itemSummaryList.contents.filter(vm.validReportItemType);
                    }
                    node.loading = false;
                });
            };
            ReportController.prototype.toggleExpansion = function (node) {
                if (!node.hasChildren) {
                    return;
                }
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
            };
            // Report structure methods
            ReportController.prototype.remove = function (scope) {
                scope.remove();
            };
            // Library folder content methods
            ReportController.prototype.validReportItemType = function (input) {
                switch (input.type) {
                    case ItemType.Query:
                    case ItemType.ListOutput:
                        return true;
                    default:
                        return false;
                }
            };
            ;
            ReportController.prototype.contentTreeDroppedCallback = function (eventInfo) {
                // Convert clone model to report node
                eventInfo.source.cloneModel.children = [];
            };
            ReportController.prototype.structureTreeAcceptCallback = function (source, destination, destinationIndex) {
                // Check for same type at same level
                if (!destination.$modelValue.every(function (sibling) { return sibling.uuid !== source.$modelValue.uuid; })) {
                    return false;
                }
                // Check for self as a parent
                var parent = destination.$nodeScope;
                while (parent) {
                    if (parent.$modelValue.uuid === source.$modelValue.uuid) {
                        return false;
                    }
                    parent = parent.$nodeScope;
                }
                return true;
            };
            ReportController.$inject = ['ReportService', 'FolderService', 'LoggerService', '$stateParams', 'AdminService', '$window'];
            return ReportController;
        })();
        angular
            .module('app.reports')
            .controller('ReportController', ReportController);
    })(reports = app.reports || (app.reports = {}));
})(app || (app = {}));
//# sourceMappingURL=report.controller.js.map