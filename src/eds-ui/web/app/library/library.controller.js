/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var library;
    (function (library) {
        var FolderType = app.models.FolderType;
        var ItemType = app.models.ItemType;
        var LibraryItemFolderModuleBase = app.blocks.LibraryItemFolderModuleBase;
        'use strict';
        var LibraryController = (function (_super) {
            __extends(LibraryController, _super);
            function LibraryController(libraryService, folderService, logger, moduleStateService, $scope, $modal, $state) {
                _super.call(this, logger, $modal, folderService, FolderType.Library);
                this.libraryService = libraryService;
                this.folderService = folderService;
                this.logger = logger;
                this.moduleStateService = moduleStateService;
                this.$scope = $scope;
                this.$modal = $modal;
                this.$state = $state;
                var state = moduleStateService.getState('library');
                if (state) {
                    this.treeData = state.treeData;
                    this.selectNode(state.selectedNode);
                }
            }
            LibraryController.prototype.actionItem = function (uuid, type, action) {
                this.saveState();
                switch (type) {
                    case ItemType.System:
                        this.$state.go('app.systemAction', { itemUuid: uuid, itemAction: action });
                        break;
                    case ItemType.Protocol:
                        this.$state.go('app.protocolAction', { itemUuid: uuid, itemAction: action });
                        break;
                    case ItemType.Query:
                        this.$state.go('app.queryAction', { itemUuid: uuid, itemAction: action });
                        break;
                    case ItemType.DataSet:
                        this.$state.go('app.dataSetAction', { itemUuid: uuid, itemAction: action });
                        break;
                    case ItemType.CodeSet:
                        this.$state.go('app.codeSetAction', { itemUuid: uuid, itemAction: action });
                        break;
                    default:
                        this.logger.error('Invalid item type', type, 'Item ' + action);
                        break;
                }
            };
            LibraryController.prototype.deleteItem = function (item) {
                var vm = this;
                vm.libraryService.deleteLibraryItem(item.uuid)
                    .then(function (result) {
                    var i = vm.itemSummaryList.contents.indexOf(item);
                    vm.itemSummaryList.contents.splice(i, 1);
                    vm.logger.success('Library item deleted', result, 'Delete item');
                })
                    .catch(function (error) {
                    vm.logger.error('Error deleting library item', error, 'Delete item');
                });
            };
            LibraryController.prototype.saveState = function () {
                var state = {
                    selectedNode: this.selectedNode,
                    treeData: this.treeData
                };
                this.moduleStateService.setState('library', state);
            };
            LibraryController.prototype.cutItem = function (item) {
                var vm = this;
                vm.libraryService.getLibraryItem(item.uuid)
                    .then(function (libraryItem) {
                    vm.moduleStateService.setState('libraryClipboard', libraryItem);
                    vm.logger.success('Item cut to clipboard', libraryItem, 'Cut');
                })
                    .catch(function (error) {
                    vm.logger.error('Error cutting to clipboard', error, 'Cut');
                });
            };
            LibraryController.prototype.copyItem = function (item) {
                var vm = this;
                vm.libraryService.getLibraryItem(item.uuid)
                    .then(function (libraryItem) {
                    vm.moduleStateService.setState('libraryClipboard', libraryItem);
                    libraryItem.uuid = null; // Force save as new
                    vm.logger.success('Item copied to clipboard', libraryItem, 'Copy');
                })
                    .catch(function (error) {
                    vm.logger.error('Error copying to clipboard', error, 'Copy');
                });
            };
            LibraryController.prototype.pasteItem = function (node) {
                var vm = this;
                var libraryItem = vm.moduleStateService.getState('libraryClipboard');
                if (libraryItem) {
                    libraryItem.folderUuid = node.uuid;
                    vm.libraryService.saveLibraryItem(libraryItem)
                        .then(function (result) {
                        vm.logger.success('Item pasted to folder', libraryItem, 'Paste');
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
            LibraryController.$inject = ['LibraryService', 'FolderService', 'LoggerService', 'ModuleStateService', '$scope', '$uibModal',
                '$state'];
            return LibraryController;
        })(LibraryItemFolderModuleBase);
        library.LibraryController = LibraryController;
        angular
            .module('app.library')
            .controller('LibraryController', LibraryController);
    })(library = app.library || (app.library = {}));
})(app || (app = {}));
//# sourceMappingURL=library.controller.js.map