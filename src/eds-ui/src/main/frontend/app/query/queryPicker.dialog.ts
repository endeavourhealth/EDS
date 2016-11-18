import {Input, Component, OnInit} from "@angular/core";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {FolderNode} from "../models/FolderNode";
import {ItemSummaryList} from "../models/ItemSummaryList";
import {FolderService} from "../folder/folder.service";
import {FolderType} from "../models/FolderType";
import {ItemType} from "../models/ItemType";
import {FolderItem} from "../models/FolderContent";
import {QuerySelection} from "../models/QuerySelection";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./queryPicker.html')
})
export class QueryPickerDialog implements OnInit {
    public static open(modalService: NgbModal,  querySelection : QuerySelection) {
        const modalRef = modalService.open(QueryPickerDialog, { backdrop : "static", size : "lg"});
        modalRef.componentInstance.resultData = querySelection;

        return modalRef;
    }

    @Input() resultData;
    treeData : FolderNode[];
    selectedNode : FolderNode;
    itemSummaryList : ItemSummaryList;

    constructor(
        protected folderService:FolderService,
        protected activeModal : NgbActiveModal) {
    }

    ngOnInit(): void {
        this.getRootFolders(FolderType.Library);
    }

    getRootFolders(folderType : FolderType) {
        var vm = this;
        vm.folderService.getFolders(folderType, null)
            .subscribe(
              (data) => {
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
                .subscribe(
                  (data) => {
                    node.nodes = data.folders;
                    // Set parent folder (not retrieved by API)
                    node.nodes.forEach((item) => { item.parentFolderUuid = node.uuid; } );
                    node.loading = false;
                });
        }
    }

    folderChanged($event) {
        this.selectNode($event.selectedFolder);
    }

    selectNode(node : FolderNode) {
        if (node === this.selectedNode) { return; }
        var vm = this;

        vm.selectedNode = node;
        node.loading = true;

        vm.folderService.getFolderContents(node.uuid)
            .subscribe(
              (data) => {
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

    getItemSummaryListContents() {
        // TODO : Reintroduce sort and filter
        if (this.itemSummaryList)
            return this.itemSummaryList.contents;
        else
            return null;
    }

    ok() {
        this.activeModal.close(this.resultData);
        console.log('OK Pressed');
    }

    cancel() {
        this.activeModal.dismiss('cancel');
        console.log('Cancel Pressed');
    }
}
