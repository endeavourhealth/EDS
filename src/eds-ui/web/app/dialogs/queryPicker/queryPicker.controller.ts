/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />
/// <reference path="../../core/library.service.ts" />

module app.dialogs {
    import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
    import IModalSettings = angular.ui.bootstrap.IModalSettings;
    import IModalService = angular.ui.bootstrap.IModalService;
    import FolderContent = app.models.FolderItem;
    import ILoggerService = app.blocks.ILoggerService;
    import itemTypeIdToString = app.models.itemTypeIdToString;
    import IScope = angular.IScope;
    import FolderItem = app.models.FolderItem;
    import ItemType = app.models.ItemType;
    import LibraryItem = app.models.LibraryItem;
    import ILibraryService = app.core.ILibraryService;
    import LibraryItemFolderModuleBase = app.blocks.LibraryItemFolderModuleBase;
    import FolderType = app.models.FolderType;
    import IFolderService = app.core.IFolderService;
    import FolderNode = app.models.FolderNode;
    import Folder = app.models.Folder;
    import ItemSummaryList = app.models.ItemSummaryList;
    import QuerySelection = app.models.QuerySelection;

    'use strict';

    export class QueryPickerController extends BaseDialogController {
        treeData : FolderNode[];
        selectedNode : FolderNode;
        itemSummaryList : ItemSummaryList;

        public static open($modal : IModalService, querySelection : QuerySelection) : IModalServiceInstance {
            var options : IModalSettings = {
                templateUrl:'app/dialogs/queryPicker/queryPicker.html',
                controller:'QueryPickerController',
                controllerAs:'queryPicker',
                size:'lg',
                backdrop: 'static',
                resolve:{
                    querySelection : () => querySelection
                }
            };

            var dialog = $modal.open(options);
            return dialog;
        }

        static $inject = ['LibraryService', 'FolderService', 'LoggerService', '$scope', '$uibModal', '$state', '$uibModalInstance', 'querySelection'];

        constructor(
            protected libraryService:ILibraryService,
            protected folderService:IFolderService,
            protected logger:ILoggerService,
            protected $scope : IScope,
            protected $modal : IModalService,
            protected $state : IStateService,
            protected $uibModalInstance : IModalServiceInstance,
            private querySelection: QuerySelection) {

            super($uibModalInstance);

            this.getRootFolders(FolderType.Library);

            this.resultData = querySelection;
        }

        getRootFolders(folderType : FolderType) {
            var vm = this;
            vm.folderService.getFolders(folderType, null)
                .then(function (data) {
                    vm.treeData = data.folders;

                    if (vm.treeData && vm.treeData.length > 0) {
                        // Set folder type (not retrieved by API)
                        vm.treeData.forEach((item) => { item.folderType = folderType; } );
                        // Expand top level by default
                        vm.toggleExpansion(vm.treeData[0]);
                    }
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

            vm.folderService.getFolderContents(node.uuid)
                .then(function(data) {
                    vm.itemSummaryList = data;
                    node.loading = false;
                });
        }

        actionItem(item : FolderItem, action : string) {
            var vm = this;
            switch (item.type) {
                case ItemType.Query:
                    var querySelection: QuerySelection = {
                        id: item.uuid,
                        name: item.name,
                        description: item.description
                    }
                    vm.resultData = querySelection;
                    this.ok();
                    break;
            }
        }

    }

    angular
        .module('app.dialogs')
        .controller('QueryPickerController', QueryPickerController);
}
