var app;
(function (app) {
    var blocks;
    (function (blocks) {
        var InputBoxController = app.dialogs.InputBoxController;
        var MessageBoxController = app.dialogs.MessageBoxController;
        var LibraryItemFolderModuleBase = (function () {
            function LibraryItemFolderModuleBase(logger, $modal, folderService, folderType) {
                this.logger = logger;
                this.$modal = $modal;
                this.folderService = folderService;
                this.folderType = folderType;
                this.getRootFolders(folderType);
            }
            LibraryItemFolderModuleBase.prototype.getRootFolders = function (folderType) {
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
            LibraryItemFolderModuleBase.prototype.toggleExpansion = function (node) {
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
            LibraryItemFolderModuleBase.prototype.selectNode = function (node) {
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
            LibraryItemFolderModuleBase.prototype.addChildFolder = function (node) {
                var vm = this;
                InputBoxController.open(vm.$modal, 'New Folder', 'Enter new folder name', 'New folder')
                    .result.then(function (result) {
                    var folder = {
                        uuid: null,
                        folderName: result,
                        folderType: vm.folderType,
                        parentFolderUuid: node.uuid,
                        contentCount: 0,
                        hasChildren: false
                    };
                    vm.folderService.saveFolder(folder)
                        .then(function (response) {
                        vm.logger.success('Folder created', response, 'New folder');
                        node.isExpanded = false;
                        node.hasChildren = true;
                        node.nodes = null;
                        vm.toggleExpansion(node);
                    })
                        .catch(function (error) {
                        vm.logger.error('Error creating folder', error, 'New folder');
                    });
                });
            };
            LibraryItemFolderModuleBase.prototype.renameFolder = function (scope) {
                var vm = this;
                var folderNode = scope.$modelValue;
                InputBoxController.open(vm.$modal, 'Rename folder', 'Enter new name for ' + folderNode.folderName, folderNode.folderName)
                    .result.then(function (newName) {
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
            };
            LibraryItemFolderModuleBase.prototype.deleteFolder = function (scope) {
                var vm = this;
                var folderNode = scope.$modelValue;
                MessageBoxController.open(vm.$modal, 'Delete folder', 'Are you sure you want to delete folder ' + folderNode.folderName + '?', 'Yes', 'No')
                    .result.then(function () {
                    vm.folderService.deleteFolder(folderNode)
                        .then(function (response) {
                        scope.remove();
                        vm.logger.success('Folder deleted', response, 'Delete folder');
                    })
                        .catch(function (error) {
                        vm.logger.error('Error deleting folder', error, 'Delete folder');
                    });
                });
            };
            return LibraryItemFolderModuleBase;
        })();
        blocks.LibraryItemFolderModuleBase = LibraryItemFolderModuleBase;
    })(blocks = app.blocks || (app.blocks = {}));
})(app || (app = {}));
//# sourceMappingURL=libraryItemFolderModuleBase.js.map