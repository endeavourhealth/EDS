/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />
/// <reference path="../../core/library.service.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var dialogs;
    (function (dialogs) {
        var ItemType = app.models.ItemType;
        var FolderType = app.models.FolderType;
        'use strict';
        var QueryPickerController = (function (_super) {
            __extends(QueryPickerController, _super);
            function QueryPickerController(libraryService, folderService, logger, $scope, $modal, $state, $uibModalInstance, querySelection) {
                _super.call(this, $uibModalInstance);
                this.libraryService = libraryService;
                this.folderService = folderService;
                this.logger = logger;
                this.$scope = $scope;
                this.$modal = $modal;
                this.$state = $state;
                this.$uibModalInstance = $uibModalInstance;
                this.querySelection = querySelection;
                this.getRootFolders(FolderType.Library);
                this.resultData = querySelection;
            }
            QueryPickerController.open = function ($modal, querySelection) {
                var options = {
                    templateUrl: 'app/dialogs/queryPicker/queryPicker.html',
                    controller: 'QueryPickerController',
                    controllerAs: 'queryPicker',
                    size: 'lg',
                    backdrop: 'static',
                    resolve: {
                        querySelection: function () { return querySelection; }
                    }
                };
                var dialog = $modal.open(options);
                return dialog;
            };
            QueryPickerController.prototype.getRootFolders = function (folderType) {
                var vm = this;
                vm.folderService.getFolders(folderType, null)
                    .then(function (data) {
                    vm.treeData = data.folders;
                    if (vm.treeData && vm.treeData.length > 0) {
                        // Set folder type (not retrieved by API)
                        vm.treeData.forEach(function (item) { item.folderType = folderType; });
                        // Expand top level by default
                        vm.toggleExpansion(vm.treeData[0]);
                    }
                });
            };
            QueryPickerController.prototype.toggleExpansion = function (node) {
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
                        // Set parent folder (not retrieved by API)
                        node.nodes.forEach(function (item) { item.parentFolderUuid = node.uuid; });
                        node.loading = false;
                    });
                }
            };
            QueryPickerController.prototype.selectNode = function (node) {
                if (node === this.selectedNode) {
                    return;
                }
                var vm = this;
                vm.selectedNode = node;
                node.loading = true;
                vm.folderService.getFolderContents(node.uuid)
                    .then(function (data) {
                    vm.itemSummaryList = data;
                    node.loading = false;
                });
            };
            QueryPickerController.prototype.actionItem = function (item, action) {
                var vm = this;
                switch (item.type) {
                    case ItemType.Query:
                        var querySelection = {
                            id: item.uuid,
                            name: item.name,
                            description: item.description
                        };
                        vm.resultData = querySelection;
                        this.ok();
                        break;
                }
            };
            QueryPickerController.$inject = ['LibraryService', 'FolderService', 'LoggerService', '$scope', '$uibModal', '$state', '$uibModalInstance', 'querySelection'];
            return QueryPickerController;
        })(dialogs.BaseDialogController);
        dialogs.QueryPickerController = QueryPickerController;
        angular
            .module('app.dialogs')
            .controller('QueryPickerController', QueryPickerController);
    })(dialogs = app.dialogs || (app.dialogs = {}));
})(app || (app = {}));
//# sourceMappingURL=queryPicker.controller.js.map