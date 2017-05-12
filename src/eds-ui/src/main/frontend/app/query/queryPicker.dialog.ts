import {Input, Component} from "@angular/core";
import {NgbModal, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {QuerySelection} from "./models/QuerySelection";
import {LibraryService, linq} from "eds-common-js";
import {FolderNode} from "eds-common-js/dist/folder/models/FolderNode";
import {ItemSummaryList} from "eds-common-js/dist/library/models/ItemSummaryList";
import {ItemType} from "eds-common-js/dist/folder/models/ItemType";
import {FolderItem} from "eds-common-js/dist/folder/models/FolderItem";

@Component({
    selector: 'ngbd-modal-content',
    template: require('./queryPicker.html')
})
export class QueryPickerDialog {
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
        protected libraryService : LibraryService,
        protected activeModal : NgbActiveModal) {
    }

    folderChanged($event) {
        this.selectNode($event.selectedFolder);
    }

    selectNode(node : FolderNode) {
        if (node === this.selectedNode) { return; }
        var vm = this;

        vm.selectedNode = node;
        node.loading = true;

        vm.libraryService.getFolderContents(node.uuid)
            .subscribe(
              (data) => {
                  data.contents = linq(data.contents)
                    .Where(t => t.type === ItemType.Query)
                    .ToArray();
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
